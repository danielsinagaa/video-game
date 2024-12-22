package com.hybrid.filter.video_game.service.filter;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import com.hybrid.filter.video_game.repository.UserRepository;
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

    // Content-Based Filtering
    public List<Game> recommendGamesBasedOnContent(Integer userId) {
        // 1. Mendapatkan semua rating dari pengguna
        List<Rating> userRatings = ratingRepository.findByUserId(userId);

        // 2. Memeriksa apakah pengguna memiliki rating game
        if (userRatings.isEmpty()) {
            return Collections.emptyList(); // Kembalikan list kosong jika tidak ada rating
        }

        // 3. Mengumpulkan ID genre dari game yang sudah di-rating
        Set<Integer> likedGenres = new HashSet<>();
        for (Rating rating : userRatings) {
            List<GameGenre> gameGenres = gameGenreRepository.findByGameId(rating.getGame().getId());
            for (GameGenre gameGenre : gameGenres) {
                likedGenres.add(gameGenre.getGenre().getId());
            }
        }

        // 4. Mencari semua game yang memiliki genre yang disukai pengguna
        List<Game> recommendedGames = new ArrayList<>();
        for (Integer genreId : likedGenres) {
            List<GameGenre> gamesByGenre = gameGenreRepository.findByGenreId(genreId);
            for (GameGenre gameGenre : gamesByGenre) {
                Game game = gameGenre.getGame();
                // Tambahkan game ke daftar rekomendasi jika belum ada
                if (!recommendedGames.contains(game)) {
                    recommendedGames.add(game);
                }
            }
        }

        // 5. Menghapus game yang sudah di-rating oleh pengguna
        Set<Integer> ratedGameIds = userRatings.stream()
                .map(rating -> rating.getGame().getId())
                .collect(Collectors.toSet());
        recommendedGames.removeIf(game -> ratedGameIds.contains(game.getId()));

        // 6. Mengurutkan game berdasarkan relevansi (bisa berdasarkan nama, tanggal rilis, atau lainnya)
        // Di sini kita hanya akan mengurutkan berdasarkan judul
        recommendedGames.sort(Comparator.comparing(Game::getTitle));

        return recommendedGames.stream()
                .limit(5) // Mengambil hanya 5 rekomendasi teratas
                .collect(Collectors.toList());
    }

}
