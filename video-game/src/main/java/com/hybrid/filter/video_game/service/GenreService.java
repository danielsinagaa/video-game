package com.hybrid.filter.video_game.service;

import com.hybrid.filter.video_game.model.dto.GameFilterDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.dto.TopGenreFilterDTO;
import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.GenreRepository;
import com.hybrid.filter.video_game.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenreService {
    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RatingRepository ratingRepository;

    public List<TopGenreFilterDTO> getTop5GamesByGenre() {
        // Fetch all genres
        List<Genre> genres = genreRepository.findAll();

        // List to store the top 5 games for each genre
        List<TopGenreFilterDTO> topGamesByGenre = new ArrayList<>();

        for (Genre genre : genres) {
            // Fetch games for the current genre
            List<Game> games = gameRepository.findByGenres(List.of(genre));


            // Calculate average rating for each game
            Map<Game, Double> gameRatings = games.stream().collect(Collectors.toMap(
                    game -> game,
                    game -> {
                        double averageRating = calculateAverageRating(game);
                        return Math.max(averageRating, 0.0); // Default to 0.0 if no rating
                    }
            ));

            // Sort games by average rating in descending order
            List<Game> sortedGames = gameRatings.entrySet().stream()
                    .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // If there are fewer than 5 rated games, add unrated games
            if (sortedGames.size() < 5) {
                games.stream()
                        .filter(game -> !sortedGames.contains(game)) // Exclude already added games
                        .sorted(Comparator.comparing(Game::getReleaseDate).reversed()) // Sort by release date as fallback
                        .limit(5 - sortedGames.size()) // Add only enough to make 5 games
                        .forEach(sortedGames::add);
            }

            // Limit the final list to 5 games
            List<Game> topGames = sortedGames.stream()
                    .limit(5)
                    .toList();
            List<GameFilterDTO> gameFilterDTOS = new ArrayList<>();
            topGames.forEach(it -> {
                GameFilterDTO gameFilterDTO = new GameFilterDTO();
                gameFilterDTO.setId(it.getId());
                gameFilterDTO.setImage(Base64.getEncoder().encodeToString(it.getGameImage()));
                gameFilterDTO.setTitle(it.getTitle());
                gameFilterDTO.setSteamLink(it.getSteamLink());
                gameFilterDTOS.add(gameFilterDTO);
            });

            // Add the top games to the result list
            TopGenreFilterDTO genreFilterDTO = new TopGenreFilterDTO();
            genreFilterDTO.setId(genre.getId());
            genreFilterDTO.setName(genre.getName());
            genreFilterDTO.setGameFilters(gameFilterDTOS);
            if (!games.isEmpty()) {
                topGamesByGenre.add(genreFilterDTO);
            }
        }

        return topGamesByGenre;
    }

    private double calculateAverageRating(Game game) {
        // Fetch all ratings for the given game
        List<Rating> ratings = ratingRepository.findByGameId(game.getId());

        // Calculate the average rating value
        return ratings.stream()
                .mapToInt(Rating::getRatingValue)
                .average()
                .orElse(0.0);
    }

    public List<GenreDTO> getAllGenresDTO() {
        List<GenreDTO> genresDTO = new ArrayList<>();
        genreRepository.findAll().forEach(genre -> {
            GenreDTO genreDTO = new GenreDTO();
            genreDTO.setId(genre.getId());
            genreDTO.setName(genre.getName());
            genreDTO.setGenreImage(genre.getGenreImage());
            genresDTO.add(genreDTO);
        });
        return genresDTO;
    }

    public Genre getGenreById(int id){
        return genreRepository.findById(id).get();
    }

    public Genre saveGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre saveGenre(String name, MultipartFile genreImage) throws IOException {
        Genre genre = new Genre();
        try {
            genre.setName(name);

            // Cek ukuran file sebelum menyimpannya
            long maxSize = 10 * 1024 * 1024; // 10 MB
            if (genreImage.getSize() > maxSize) {
                throw new IOException("File size exceeds maximum allowed size of 10 MB");
            }

            // Mengonversi gambar menjadi byte[] dan menyimpannya dalam kolom genre_image
            byte[] genreImageBytes = genreImage.getBytes();
            genre.setGenreImage(genreImageBytes); // Set gambar dalam bentuk byte[]

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to save genre image", e);
        }
        return genreRepository.save(genre);
    }

}
