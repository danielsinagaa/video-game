package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Collaborative {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔹 Atur faktor random di sini — bisa diubah atau dikomentari jika tidak ingin random sama sekali
    private static final double RANDOM_FACTOR = 0.15; // 0.15 = variasi 15%

    public List<Game> recommendGamesBasedOnCollaboration(Integer userId) {
        if (userId == null) userId = 0;

        // 1. Mendapatkan semua rating dari pengguna
        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        if (userRatings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Simpan rating user dalam Map
        Map<Integer, Integer> userRatingMap = userRatings.stream()
                .collect(Collectors.toMap(r -> r.getGame().getId(), Rating::getRatingValue));

        // 3. Kumpulkan semua rating dari seluruh user lain
        List<Rating> allRatings = ratingRepository.findAll();
        Map<Integer, List<Rating>> similarUsersRatings = new HashMap<>();

        for (Rating rating : allRatings) {
            if (!rating.getUser().getId().equals(userId)) {
                Integer otherUserId = rating.getUser().getId();
                if (userRatingMap.containsKey(rating.getGame().getId())) {
                    similarUsersRatings.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(rating);
                }
            }
        }

        // 4. Hitung kesamaan antar user
        Map<Integer, Double> userSimilarityScores = new HashMap<>();
        for (Map.Entry<Integer, List<Rating>> entry : similarUsersRatings.entrySet()) {
            double similarityScore = calculateSimilarity(userRatingMap, entry.getValue());
            if (similarityScore > 0) {
                userSimilarityScores.put(entry.getKey(), similarityScore);
            }
        }

        // 5. Hitung skor rekomendasi
        Map<Integer, Double> recommendedGameScores = new HashMap<>();
        Random random = new Random();

        for (Map.Entry<Integer, Double> entry : userSimilarityScores.entrySet()) {
            Integer similarUserId = entry.getKey();
            double score = entry.getValue();

            List<Rating> similarUserRatings = ratingRepository.findByUserId(similarUserId);
            for (Rating rating : similarUserRatings) {
                if (!userRatingMap.containsKey(rating.getGame().getId())) {
                    double baseScore = rating.getRatingValue() * score;

                    // 🎲 Tambahkan unsur random di sini
                    double randomModifier = 1.0 + (random.nextDouble() * RANDOM_FACTOR - (RANDOM_FACTOR / 2));
                    double finalScore = baseScore * randomModifier;

                    recommendedGameScores.merge(rating.getGame().getId(), finalScore, Double::sum);
                }
            }
        }

        // 6. Urutkan game berdasarkan skor total (yang sudah di-random sedikit)
        List<Game> recommendedGames = recommendedGameScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(entry -> gameRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());

        return recommendedGames;
    }

    // Fungsi cosine similarity (tanpa diubah)
    private double calculateSimilarity(Map<Integer, Integer> userRatingMap, List<Rating> otherUserRatings) {
        double dotProduct = 0.0;
        double userMagnitude = 0.0;
        double otherUserMagnitude = 0.0;

        for (Rating otherRating : otherUserRatings) {
            Integer gameId = otherRating.getGame().getId();
            if (userRatingMap.containsKey(gameId)) {
                int userRating = userRatingMap.get(gameId);
                int otherRatingValue = otherRating.getRatingValue();
                dotProduct += userRating * otherRatingValue;
                userMagnitude += Math.pow(userRating, 2);
                otherUserMagnitude += Math.pow(otherRatingValue, 2);
            }
        }

        if (userMagnitude == 0.0 || otherUserMagnitude == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(userMagnitude) * Math.sqrt(otherUserMagnitude));
    }
}
