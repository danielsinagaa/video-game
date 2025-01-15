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

    public List<Game> recommendGamesBasedOnCollaboration(Integer userId) {
        if (userId == null) {
            userId = 0;
        }

        // 1. Mendapatkan semua rating dari pengguna yang meminta rekomendasi
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // 2. Memeriksa apakah pengguna memiliki rating game
        if (userRatings.isEmpty()) {
            return Collections.emptyList(); // Kembalikan list kosong jika tidak ada rating
        }

        // 3. Menyimpan ID game dan rating value dari pengguna
        Map<Integer, Integer> userRatingMap = userRatings.stream()
                .collect(Collectors.toMap(rating -> rating.getGame().getId(), Rating::getRatingValue));

        // 4. Menemukan pengguna lain yang mirip
        List<Rating> allRatings = ratingRepository.findAll();
        Map<Integer, List<Rating>> similarUsersRatings = new HashMap<>();

        for (Rating rating : allRatings) {
            // Hanya membandingkan dengan pengguna lain
            if (!rating.getUser().getId().equals(userId)) {
                Integer otherUserId = rating.getUser().getId();
                // Cek kesamaan rating
                if (userRatingMap.containsKey(rating.getGame().getId())) {
                    similarUsersRatings.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(rating);
                }
            }
        }

        // 5. Mencari pengguna dengan rating yang sama
        Map<Integer, Double> userSimilarityScores = new HashMap<>();

        for (Map.Entry<Integer, List<Rating>> entry : similarUsersRatings.entrySet()) {
            Integer otherUserId = entry.getKey();
            List<Rating> otherUserRatings = entry.getValue();

            double similarityScore = calculateSimilarity(userRatingMap, otherUserRatings);
            if (similarityScore > 0) {
                userSimilarityScores.put(otherUserId, similarityScore);
            }
        }

        // 6. Mengumpulkan game yang direkomendasikan dari pengguna mirip
        Map<Integer, Double> recommendedGameScores = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : userSimilarityScores.entrySet()) {
            Integer similarUserId = entry.getKey();
            double score = entry.getValue();

            List<Rating> similarUserRatings = ratingRepository.findByUserId(similarUserId);
            for (Rating rating : similarUserRatings) {
                if (!userRatingMap.containsKey(rating.getGame().getId())) { // Hanya game yang belum dinilai
                    recommendedGameScores.merge(rating.getGame().getId(), rating.getRatingValue() * score, Double::sum);
                }
            }
        }

        // 7. Mengurutkan rekomendasi berdasarkan skor total dan membatasi hasil menjadi 5 rekomendasi
        return recommendedGameScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(5) // Mengambil hanya 5 rekomendasi teratas
                .map(entry -> gameRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull) // Menghapus rekomendasi yang null
                .collect(Collectors.toList());
    }

    // Dummy method for user existence check
    private boolean userExists(Integer userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Menghitung skor kesamaan antara pengguna yang memberikan rating.
     *
     * @param userRatingMap Peta yang berisi ID game dan rating yang diberikan oleh pengguna pertama.
     * @param otherUserRatings Daftar rating yang diberikan oleh pengguna kedua.
     * @return Skor kesamaan antara dua pengguna.
     */
    private double calculateSimilarity(Map<Integer, Integer> userRatingMap, List<Rating> otherUserRatings) {
        double dotProduct = 0.0;
        double userMagnitude = 0.0;
        double otherUserMagnitude = 0.0;

        // Menghitung dot product dan magnitudes
        for (Rating otherRating : otherUserRatings) {
            Integer gameId = otherRating.getGame().getId();

            // Hanya jika kedua pengguna telah memberi rating pada game yang sama
            if (userRatingMap.containsKey(gameId)) {
                // Ambil rating dari pengguna asli
                int userRating = userRatingMap.get(gameId);
                int otherRatingValue = otherRating.getRatingValue();

                // Menghitung dot product
                dotProduct += userRating * otherRatingValue;

                // Menghitung magnitudes
                userMagnitude += Math.pow(userRating, 2);
                otherUserMagnitude += Math.pow(otherRatingValue, 2);
            }
        }

        // Jika salah satu magnitude adalah nol, maka tidak ada kesamaan
        if (userMagnitude == 0.0 || otherUserMagnitude == 0.0) {
            return 0.0;
        }

        // Menghitung dan mengembalikan cosine similarity
        return dotProduct / (Math.sqrt(userMagnitude) * Math.sqrt(otherUserMagnitude));
    }

}
