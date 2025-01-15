package com.hybrid.filter.video_game.service.filter.genre;

import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GenreRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentBasedGenre {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> recommendGenresBasedOnContent(Integer userId) {
        // 1. Mendapatkan semua rating dari pengguna
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // 2. Memeriksa apakah pengguna memiliki rating game
        if (userRatings.isEmpty()) {
            return Collections.emptyList(); // Kembalikan list kosong jika tidak ada rating
        }

        // 3. Mengumpulkan genre dari game yang di-rating tinggi oleh pengguna
        Map<Integer, Double> genreScores = new HashMap<>();
        for (Rating rating : userRatings) {
            if (rating.getRatingValue() >= 4) { // Hanya game dengan rating >= 4
                List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
                for (GameGenre gameGenre : gameGenres) {
                    genreScores.merge(gameGenre.getGenre().getId(),
                            (double) rating.getRatingValue(),
                            Double::sum);
                }
            }
        }

        // 4. Mengurutkan genre berdasarkan skor dan mengembalikan rekomendasi
        return genreScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(5) // Mengambil 5 genre teratas
                .map(entry -> genreRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull) // Hapus entri null
                .collect(Collectors.toList());
    }
}

