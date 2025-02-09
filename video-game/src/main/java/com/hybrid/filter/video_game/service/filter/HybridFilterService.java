package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
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

    private static final double COLLABORATIVE_WEIGHT = 0.7;
    private static final double CONTENT_BASED_WEIGHT = 0.3;
    private static final int MINIMUM_RATINGS_THRESHOLD = 5; // Batas minimum rating untuk pembobotan

    public Set<Game> hybridFilter(Integer userId) {
        List<Game> collaborativeFilter = collaborative.recommendGamesBasedOnCollaboration(userId);
        List<Game> contentBasedFilter = contentBased.recommendGamesBasedOnContent(userId);

        // Jika kedua metode tidak menghasilkan rekomendasi, gunakan rekomendasi populer
        if (collaborativeFilter.isEmpty() && contentBasedFilter.isEmpty()) {
            return recommendPopularGames();
        }

        // Mengecek apakah pengguna memiliki rating yang cukup untuk menggunakan pembobotan
        boolean hasEnoughRatings = ratingRepository.findByUserId(userId).size() >= MINIMUM_RATINGS_THRESHOLD;

        Map<Game, Double> weightedScores = new HashMap<>();

        // Menambahkan skor dari Collaborative Filtering
        for (int i = 0; i < collaborativeFilter.size(); i++) {
            Game game = collaborativeFilter.get(i);
            double score = hasEnoughRatings ? (COLLABORATIVE_WEIGHT * (collaborativeFilter.size() - i)) : 1.0;
            weightedScores.merge(game, score, Double::sum);
        }

        // Menambahkan skor dari Content-Based Filtering
        for (int i = 0; i < contentBasedFilter.size(); i++) {
            Game game = contentBasedFilter.get(i);
            double score = hasEnoughRatings ? (CONTENT_BASED_WEIGHT * (contentBasedFilter.size() - i)) : 1.0;
            weightedScores.merge(game, score, Double::sum);
        }

        // Mengurutkan berdasarkan skor tertinggi dan mengambil 5 rekomendasi terbaik
        return weightedScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    private Set<Game> recommendPopularGames() {
        List<Game> allGames = gameRepository.findAll();
        return allGames.stream()
                .sorted(Comparator.comparingDouble(this::calculateGameScore).reversed())
                .limit(5).collect(Collectors.toSet());
    }

    public double calculateGameScore(Game game) {
        List<Rating> ratings = ratingRepository.findByGameId(game.getId());
        long ratingCount = ratings.size();
        if (ratingCount == 0) return 0;

        double averageRating = ratings.stream().mapToInt(Rating::getRatingValue).average().orElse(0);
        return averageRating * Math.log1p(ratingCount);
    }
}
