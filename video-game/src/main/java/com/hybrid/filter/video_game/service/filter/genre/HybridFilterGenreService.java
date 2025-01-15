package com.hybrid.filter.video_game.service.filter.genre;

import com.hybrid.filter.video_game.model.dto.GameFilterDTO;
import com.hybrid.filter.video_game.model.dto.GenreFilterDTO;
import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.GameGenre;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.repository.GenreRepository;
import com.hybrid.filter.video_game.service.RatingService;
import com.hybrid.filter.video_game.service.filter.HybridFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridFilterGenreService {
    @Autowired
    private CollaborativeGenre collaborativeGenre;

    @Autowired
    private ContentBasedGenre contentBasedGenre;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private HybridFilterService hybridFilterService;

    @Autowired
    private RatingService ratingService;

    public List<GenreFilterDTO> genreFilter(Integer userId) {
        Set<Genre> hybridGenre = hybridFilterGenre(userId);
        List<GenreFilterDTO> filteredGenre = new ArrayList<>();
        hybridGenre.forEach(genre -> {
            GenreFilterDTO genreFilterDTO = new GenreFilterDTO();
            genreFilterDTO.setName(genre.getName());
            List<Game> games = gameRepository.findByGenres(List.of(genre));

            List<GameFilterDTO> gameFilterDTOS = new ArrayList<>();
            games.forEach(game -> {
                GameFilterDTO gameFilterDTO = new GameFilterDTO();
                gameFilterDTO.setGenres(game.getGenres().stream().map(Genre::getName).collect(Collectors.joining(", ")));
                gameFilterDTO.setId(game.getId());
                gameFilterDTO.setTitle(game.getTitle());
                gameFilterDTO.setDeveloper(game.getDeveloper());
                gameFilterDTO.setDescription(game.getDescription());
                gameFilterDTO.convertRupiah(game.getPrice());
                gameFilterDTO.setSteamLink(game.getSteamLink());
                Date releaseDate = game.getReleaseDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(releaseDate);
                gameFilterDTO.setDateString(formattedDate);
                gameFilterDTO.setImage(Base64.getEncoder().encodeToString(game.getGameImage()));
                List<Rating> ratings = ratingService.getRating(game.getId());
                if(ratings.isEmpty()) {
                    gameFilterDTO.setRating(0.0);
                    gameFilterDTO.setRatingSum(0);
                } else {
                    Double rating = 0.0;
                    for (Rating rate : ratings) {
                        rating += rate.getRatingValue();
                    }
                    gameFilterDTO.setRating(rating/ratings.size());
                    gameFilterDTO.setRatingSum(ratings.size());
                }
                gameFilterDTOS.add(gameFilterDTO);
            });
            genreFilterDTO.setGameFilters(gameFilterDTOS);
            filteredGenre.add(genreFilterDTO);
        });
        return filteredGenre;
    }

    public Set<Genre> hybridFilterGenre(Integer userId) {
        List<Genre> collaborative = collaborativeGenre.recommendGenresBasedOnCollaboration(userId);
        List<Genre> contentBased = contentBasedGenre.recommendGenresBasedOnContent(userId);

        if (collaborative.isEmpty() && contentBased.isEmpty()){
            return new HashSet<>(recommendPopularGenres());
        }

        Set<Genre> genres = new HashSet<>(collaborative);
        genres.addAll(contentBased);

        return genres;
    }

    private List<Genre> recommendPopularGenres() {
        // Mendapatkan semua genre
        List<Genre> allGenres = genreRepository.findAll();

        // Mengurutkan genre berdasarkan skor popularitas
        return allGenres.stream()
                .sorted((genre1, genre2) -> Double.compare(
                        calculateGenreScore(genre2), calculateGenreScore(genre1)))
                .limit(5) // Mengambil 5 genre teratas
                .collect(Collectors.toList());
    }

    // Menghitung skor popularitas genre berdasarkan rata-rata rating dan jumlah game
    private double calculateGenreScore(Genre genre) {
        // Mendapatkan semua game dalam genre tertentu
        List<GameGenre> gamesInGenre = gameGenreRepository.findByGenreId(genre.getId());

        if (gamesInGenre.isEmpty()) {
            return 0; // Jika genre tidak memiliki game, skornya adalah 0
        }

        // Menghitung total skor genre berdasarkan game yang ada
        double totalScore = gamesInGenre.stream()
                .map(GameGenre::getGame) // Ambil game dari GameGenre
                .mapToDouble(game -> hybridFilterService.calculateGameScore(game)) // Hitung skor setiap game
                .sum();

        return totalScore; // Kembalikan total skor genre
    }
}
