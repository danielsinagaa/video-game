package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public Set<Game> hybridFilter(Integer userId){
        List<Game> collaborativeFilter = collaborative.recommendGamesBasedOnCollaboration(userId);
        List<Game> contentBasedFilter = contentBased.recommendGamesBasedOnContent(userId);

        if (collaborativeFilter.isEmpty() && contentBasedFilter.isEmpty()){
            return new HashSet<>(recommendPopularGames());
        }

        Set<Game> games = new HashSet<>(collaborativeFilter);
        games.addAll(contentBasedFilter);

        return games;
    }

    private List<Game> recommendPopularGames() {
        List<Game> allGames = gameRepository.findAll();
        return allGames.stream()
                .sorted((game1, game2) -> Double.compare(
                        calculateGameScore(game2), calculateGameScore(game1)))
                .limit(5) // Ambil 5 game teratas
                .collect(Collectors.toList());
    }

    // Menghitung skor popularitas game berdasarkan rata-rata rating dan jumlah rating
    private double calculateGameScore(Game game) {
        List<Rating> ratings = ratingRepository.findByGameId(game.getId());
        long ratingCount = ratings.size();

        if (ratingCount == 0) {
            return 0; // Jika tidak ada rating, skor adalah 0
        }

        // Menghitung rata-rata rating
        double averageRating = ratings.stream()
                .mapToInt(Rating::getRatingValue)
                .average()
                .orElse(0);

        // Kombinasikan rata-rata rating dengan jumlah rating, misalnya dengan mengalikan jumlah rating
        return averageRating * Math.log1p(ratingCount); // log1p untuk menghindari masalah dengan ratingCount == 0
    }
}
