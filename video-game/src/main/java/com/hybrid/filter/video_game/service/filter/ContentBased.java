package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.GuestPreference;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.service.GuestPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentBased {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GuestPreferenceService guestPreferenceService;

    /**
     * Memberikan rekomendasi game berdasarkan:
     *  - Rating user (jika ada)
     *  - Atau genre preference dari GuestPreference (jika user belum punya rating)
     */
    public List<Game> recommendGamesBasedOnContent(Integer userId) {
        if (userId == null) {
            userId = 0;
        }

        // 1. Ambil semua rating user
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        Set<Integer> likedGenres = new HashSet<>();

        // 2. Jika user punya rating → ambil genre dari game yang dirating
        if (!userRatings.isEmpty()) {
            for (Rating rating : userRatings) {
                List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
                for (GameGenre gameGenre : gameGenres) {
                    likedGenres.add(gameGenre.getGenre().getId());
                }
            }
        } else {
            // 3. Jika tidak ada rating → ambil preferensi genre dari GuestPreference
            Optional<GuestPreference> prefOpt = guestPreferenceService.findByUserId(userId);

            if (prefOpt.isPresent()) {
                GuestPreference pref = prefOpt.get();
                String genreString = pref.getGenres();
                if (genreString != null && !genreString.isBlank()) {
                    Arrays.stream(genreString.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .forEach(likedGenres::add);
                }
            }
        }

        // 4. Jika masih kosong, berarti tidak ada data preferensi
        if (likedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        // 5. Ambil semua game yang punya genre sesuai preferensi
        List<Game> recommendedGames = new ArrayList<>();
        for (Integer genreId : likedGenres) {
            List<GameGenre> gamesByGenre = gameGenreRepository.findByGenreId(genreId);
            for (GameGenre gg : gamesByGenre) {
                Game game = gg.getGame();
                if (!recommendedGames.contains(game)) {
                    recommendedGames.add(game);
                }
            }
        }

        // 6. Hapus game yang sudah di-rating user (kalau ada)
        if (!userRatings.isEmpty()) {
            Set<Integer> ratedIds = userRatings.stream()
                    .map(r -> r.getGame().getId())
                    .collect(Collectors.toSet());
            recommendedGames.removeIf(g -> ratedIds.contains(g.getId()));
        }

        // 7. Urutkan hasil rekomendasi (contoh: berdasarkan judul)
        recommendedGames.sort(Comparator.comparing(Game::getTitle));

        // 8. Batasi hasil (misal 5 teratas)
        return recommendedGames.stream()
                .limit(5)
                .collect(Collectors.toList());
    }
}
