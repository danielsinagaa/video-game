package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.GuestPreference;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.UserRepository;
import com.hybrid.filter.video_game.service.GuestPreferenceService;
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

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GuestPreferenceService guestPreferenceService;

    // 🔹 Unsur variasi random — bisa dikomentari jika tidak diinginkan
    private static final double RANDOM_FACTOR = 0.15;

    public List<Game> recommendGamesBasedOnCollaboration(Integer userId) {
        if (userId == null) userId = 0;

        // 1️⃣ Ambil rating user
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // 2️⃣ Jika user belum punya rating → fallback ke preferensi genre
        if (userRatings.isEmpty()) {
            Optional<GuestPreference> prefOpt = guestPreferenceService.findByUserId(userId);
            if (prefOpt.isPresent()) {
                GuestPreference pref = prefOpt.get();
                String genreString = pref.getGenres();

                if (genreString != null && !genreString.isBlank()) {
                    Set<Integer> likedGenres = Arrays.stream(genreString.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toSet());

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

                    // Tambahkan sedikit variasi random agar hasil tidak monoton
                    Random random = new Random();
                    recommendedGames.sort(Comparator.comparing(Game::getTitle));
                    Collections.shuffle(recommendedGames, random);

                    return recommendedGames.stream()
                            .limit(5)
                            .collect(Collectors.toList());
                }
            }

            // Tidak punya rating dan preferensi → return kosong
            return Collections.emptyList();
        }

        // 3️⃣ Jika punya rating → lanjut collaborative filtering
        Map<Integer, Integer> userRatingMap = userRatings.stream()
                .collect(Collectors.toMap(r -> r.getGame().getId(), Rating::getRatingValue));

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

        Map<Integer, Double> userSimilarityScores = new HashMap<>();
        for (Map.Entry<Integer, List<Rating>> entry : similarUsersRatings.entrySet()) {
            double similarityScore = calculateSimilarity(userRatingMap, entry.getValue());
            if (similarityScore > 0) {
                userSimilarityScores.put(entry.getKey(), similarityScore);
            }
        }

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

        List<Game> recommendedGames = recommendedGameScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(entry -> gameRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());

        return recommendedGames;
    }

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
