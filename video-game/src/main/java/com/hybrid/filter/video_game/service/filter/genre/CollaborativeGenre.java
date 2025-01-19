package com.hybrid.filter.video_game.service.filter.genre;

import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GenreRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollaborativeGenre {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> recommendGenresBasedOnCollaboration(Integer userId) {

        // 1. Mendapatkan semua rating dari pengguna
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // 2. Memeriksa apakah pengguna memiliki rating
        if (userRatings.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Membuat peta genre yang disukai pengguna
        Map<Integer, Integer> userGenreScores = new HashMap<>();
        for (Rating rating : userRatings) {
            List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
            for (GameGenre gameGenre : gameGenres) {
                userGenreScores.merge(gameGenre.getGenre().getId(), rating.getRatingValue(), Integer::sum);
            }
        }

        // 4. Mencari pengguna mirip berdasarkan genre
        Map<Integer, Double> userSimilarityScores = new HashMap<>();
        List<Rating> allRatings = ratingRepository.findAll();
        Map<Integer, List<Rating>> similarUsersRatings = new HashMap<>();

        for (Rating rating : allRatings) {
            if (!rating.getUser().getId().equals(userId)) {
                Integer otherUserId = rating.getUser().getId();
                List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());

                for (GameGenre gameGenre : gameGenres) {
                    if (userGenreScores.containsKey(gameGenre.getGenre().getId())) {
                        similarUsersRatings.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(rating);
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<Rating>> entry : similarUsersRatings.entrySet()) {
            Integer otherUserId = entry.getKey();
            List<Rating> otherUserRatings = entry.getValue();
            double similarityScore = calculateGenreSimilarity(userGenreScores, otherUserRatings);

            if (similarityScore > 0) {
                userSimilarityScores.put(otherUserId, similarityScore);
            }
        }

        // 5. Mengumpulkan genre yang direkomendasikan dari pengguna mirip
        Map<Integer, Double> recommendedGenreScores = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : userSimilarityScores.entrySet()) {
            Integer similarUserId = entry.getKey();
            double similarityScore = entry.getValue();

            List<Rating> similarUserRatings = ratingRepository.findByUserId(similarUserId);
            for (Rating rating : similarUserRatings) {
                List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
                for (GameGenre gameGenre : gameGenres) {
                    if (!userGenreScores.containsKey(gameGenre.getGenre().getId())) {
                        recommendedGenreScores.merge(gameGenre.getGenre().getId(),
                                rating.getRatingValue() * similarityScore,
                                Double::sum);
                    }
                }
            }
        }

        // 6. Mengurutkan rekomendasi berdasarkan skor total
        return recommendedGenreScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(5) // Mengambil 5 genre teratas
                .map(entry -> genreRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull) // Menghapus rekomendasi null
                .collect(Collectors.toList());
    }

    private boolean userExists(Integer userId) {
        return userRepository.existsById(userId);
    }

    private double calculateGenreSimilarity(Map<Integer, Integer> userGenreScores, List<Rating> otherUserRatings) {
        double dotProduct = 0.0;
        double userMagnitude = 0.0;
        double otherUserMagnitude = 0.0;

        for (Rating rating : otherUserRatings) {
            List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
            for (GameGenre gameGenre : gameGenres) {
                Integer genreId = gameGenre.getGenre().getId();
                if (userGenreScores.containsKey(genreId)) {
                    int userScore = userGenreScores.get(genreId);
                    int otherScore = rating.getRatingValue();

                    dotProduct += userScore * otherScore;
                    userMagnitude += Math.pow(userScore, 2);
                    otherUserMagnitude += Math.pow(otherScore, 2);
                }
            }
        }

        if (userMagnitude == 0.0 || otherUserMagnitude == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(userMagnitude) * Math.sqrt(otherUserMagnitude));
    }
}

