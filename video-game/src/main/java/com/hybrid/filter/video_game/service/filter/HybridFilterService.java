package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.model.entity.GuestPreference;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.GuestPreferenceRepository;
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
    private GuestPreferenceRepository guestPreferenceRepository;

    private static final double COLLABORATIVE_WEIGHT = 0.7;
    private static final double CONTENT_BASED_WEIGHT = 0.3;
    private static final double GENRE_MATCH_BONUS = 0.2; // Bonus untuk genre yang disukai
    private static final int MINIMUM_RATINGS_THRESHOLD = 5;

    public Set<Game> hybridFilter(Integer userId) {
        List<Game> collaborativeFilter = collaborative.recommendGamesBasedOnCollaboration(userId);
        List<Game> contentBasedFilter = contentBased.recommendGamesBasedOnContent(userId);

        // Jika kedua metode kosong, fallback ke populer
        if (collaborativeFilter.isEmpty() && contentBasedFilter.isEmpty()) {
            return recommendPopularGames();
        }

        boolean hasEnoughRatings = ratingRepository.findByUserId(userId).size() >= MINIMUM_RATINGS_THRESHOLD;

        Map<Game, Double> weightedScores = new HashMap<>();

        // 💠 Tambahkan skor dari Collaborative Filtering
        for (int i = 0; i < collaborativeFilter.size(); i++) {
            Game game = collaborativeFilter.get(i);
            double score = hasEnoughRatings ? (COLLABORATIVE_WEIGHT * (collaborativeFilter.size() - i)) : 1.0;
            weightedScores.merge(game, score, Double::sum);
        }

        // 💠 Tambahkan skor dari Content-Based Filtering
        for (int i = 0; i < contentBasedFilter.size(); i++) {
            Game game = contentBasedFilter.get(i);
            double score = hasEnoughRatings ? (CONTENT_BASED_WEIGHT * (contentBasedFilter.size() - i)) : 1.0;
            weightedScores.merge(game, score, Double::sum);
        }

        // 💠 Tambahkan pengaruh preferensi genre
        applyGenrePreferenceBonus(userId, weightedScores);

        // 💠 (Opsional) Tambahkan variasi random ringan
        applyControlledRandomness(weightedScores); // <-- Comment baris ini jika tidak ingin random

        // 💠 Urutkan berdasarkan skor tertinggi dan ambil 5 teratas
        return weightedScores.entrySet().stream()
                .sorted(Map.Entry.<Game, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void applyGenrePreferenceBonus(Integer userId, Map<Game, Double> weightedScores) {
        Optional<GuestPreference> prefOpt = guestPreferenceRepository.findByUserId(Long.valueOf(userId));
        if (prefOpt.isEmpty()) return;

        GuestPreference pref = prefOpt.get();
        if (pref.getGenres() == null || pref.getGenres().isEmpty()) return;

        Set<Integer> preferredGenreIds = Arrays.stream(pref.getGenres().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        // Ambil semua game yang memiliki genre yang sesuai
        List<Game> matchingGenreGames = gameRepository.findAll().stream()
                .filter(g -> g.getGenres() != null && g.getGenres().stream()
                        .anyMatch(genre -> preferredGenreIds.contains(genre.getId())))
                .collect(Collectors.toList());

        for (Game game : matchingGenreGames) {
            weightedScores.merge(game, GENRE_MATCH_BONUS, Double::sum);
        }
    }

    private Set<Game> recommendPopularGames() {
        List<Game> allGames = gameRepository.findAll();
        return allGames.stream()
                .sorted(Comparator.comparingDouble(this::calculateGameScore).reversed())
                .limit(5)
                .collect(Collectors.toSet());
    }

    public double calculateGameScore(Game game) {
        List<Rating> ratings = ratingRepository.findByGameId(game.getId());
        long ratingCount = ratings.size();
        if (ratingCount == 0) return 0;

        double averageRating = ratings.stream()
                .mapToInt(Rating::getRatingValue)
                .average()
                .orElse(0);

        return averageRating * Math.log1p(ratingCount);
    }

    /**
     * Memberi variasi kecil (±5%) pada skor agar hasil rekomendasi tidak selalu identik.
     * Jika tidak ingin efek random, cukup comment pemanggilannya.
     */
    private void applyControlledRandomness(Map<Game, Double> weightedScores) {
        Random random = new Random(System.currentTimeMillis() / (1000 * 60)); // seed berubah tiap menit
        for (Map.Entry<Game, Double> entry : weightedScores.entrySet()) {
            double baseScore = entry.getValue();
            double noiseFactor = 0.95 + (random.nextDouble() * 0.1); // ±5%
            weightedScores.put(entry.getKey(), baseScore * noiseFactor);
        }
    }

}
