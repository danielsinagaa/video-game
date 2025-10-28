package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.GuestPreference;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.service.GuestPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridFilterService {

    @Autowired
    private Collaborative collaborative;

    @Autowired
    private ContentBased contentBased;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GuestPreferenceService guestPreferenceService;

    public Set<Game> hybridFilter(Integer userId) {
        // 1️⃣ Ambil hasil dari dua filter utama
        List<Game> collaborativeFilter = collaborative.recommendGamesBasedOnCollaboration(userId);
        List<Game> contentBasedFilter = contentBased.recommendGamesBasedOnContent(userId);

        // 2️⃣ Ambil preferensi genre user (jika ada)
        Set<Integer> likedGenres = new HashSet<>();
        Optional<GuestPreference> prefOpt = guestPreferenceService.findByUserId(userId);

        if (prefOpt.isPresent()) {
            GuestPreference pref = prefOpt.get();
            String genreString = pref.getGenres();
            if (genreString != null && !genreString.isBlank()) {
                likedGenres = Arrays.stream(genreString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet());
            }
        }

        // 3️⃣ Jika semua kosong, fallback ke popular games
        if (collaborativeFilter.isEmpty() && contentBasedFilter.isEmpty() && likedGenres.isEmpty()) {
            return new HashSet<>(recommendPopularGames());
        }

        // 4️⃣ Gabungkan hasil dari kedua metode
        Set<Game> combined = new HashSet<>(collaborativeFilter);
        combined.addAll(contentBasedFilter);

        // 5️⃣ Jika ada genre preference, tambahkan game dari genre tersebut
        if (!likedGenres.isEmpty()) {
            List<Game> genreMatchedGames = new ArrayList<>();

            for (Integer genreId : likedGenres) {
                List<GameGenre> gamesByGenre = gameGenreRepository.findByGenreId(genreId);
                for (GameGenre gg : gamesByGenre) {
                    genreMatchedGames.add(gg.getGame());
                }
            }

            // Tambahkan game yang belum ada dalam hasil
            combined.addAll(genreMatchedGames);
        }

        // 6️⃣ Buat daftar terurut berdasarkan relevansi:
        // - Game yang memiliki genre preference akan diutamakan.
        // - Sisanya berdasarkan popularitas.
        final Set<Integer> likedGenresFinal = likedGenres;

        List<Game> sortedGames = combined.stream()
                .sorted((g1, g2) -> {
                    boolean g1Liked = hasLikedGenre(g1, likedGenresFinal);
                    boolean g2Liked = hasLikedGenre(g2, likedGenresFinal);

                    if (g1Liked && !g2Liked) return -1;
                    if (!g1Liked && g2Liked) return 1;

                    // Jika sama, urutkan berdasarkan skor popularitas
                    double score1 = calculateGameScore(g1);
                    double score2 = calculateGameScore(g2);
                    return Double.compare(score2, score1);
                })
                .limit(5) // Ambil maksimal 5
                .collect(Collectors.toList());

        return new LinkedHashSet<>(sortedGames);
    }

    private boolean hasLikedGenre(Game game, Set<Integer> likedGenres) {
        if (likedGenres.isEmpty()) return false;
        List<GameGenre> gameGenres = gameGenreRepository.findByGameId(game.getId());
        return gameGenres.stream().anyMatch(g -> likedGenres.contains(g.getGenre().getId()));
    }

    private List<Game> recommendPopularGames() {
        List<Game> allGames = gameRepository.findAll();
        return allGames.stream()
                .sorted((game1, game2) -> Double.compare(
                        calculateGameScore(game2), calculateGameScore(game1)))
                .limit(5)
                .collect(Collectors.toList());
    }

    // 🔹 Skor popularitas = rata-rata rating × log(jumlah rating)
    public double calculateGameScore(Game game) {
        List<Rating> ratings = ratingRepository.findByGameId(game.getId());
        long ratingCount = ratings.size();

        if (ratingCount == 0) {
            return 0;
        }

        double averageRating = ratings.stream()
                .mapToInt(Rating::getRatingValue)
                .average()
                .orElse(0);

        return averageRating * Math.log1p(ratingCount);
    }
}
