package com.hybrid.filter.video_game.service;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Genre;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.service.filter.HybridFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    RatingService ratingService;

    @Autowired
    GenreService genreService;

    @Autowired
    HybridFilterService hybridFilterService;

    public void removeGame(int id){
        gameRepository.deleteById(id);
    }

    public List<Integer> getReleaseYears() {
        List<Game> games = gameRepository.findAll();
        return games.stream()
                .map(game -> game.getReleaseDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().getYear())
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<GameDTO> findGamesByYear(int year) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        Date startDate = calendar.getTime();

        calendar.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endDate = calendar.getTime();

        List<GameDTO> gameResponse = new ArrayList<>();
        List<Game> filteredGames = gameRepository.findByReleaseDateBetween(startDate, endDate);

        return getGameDTOS(gameResponse, filteredGames);
    }

    public List<GameDTO> getFreeGames() {
        List<GameDTO> gameResponse = new ArrayList<>();
        List<Game> games = gameRepository.findByPrice(0.0);
        return getGameDTOS(gameResponse, games);
    }

    public List<GameDTO> getGamesByGenreId(int genreId){
        Genre genre = genreService.getGenreById(genreId);

        List<GameDTO> gameResponse = new ArrayList<>();
        List<Game> games = gameRepository.findByGenres(List.of(genre));
        return getGameDTOS(gameResponse, games);
    }


    public Game findGame(int gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        return game;
    }

    public GameDTO getGameById(int gameId){
        Game game = gameRepository.findById(gameId).orElse(null);
        GameDTO gameDTO = new GameDTO();
        gameDTO.setGenres(game.getGenres().stream().map(Genre::getName).collect(Collectors.joining(", ")));
        gameDTO.setId(game.getId());
        gameDTO.setTitle(game.getTitle());
        gameDTO.setDeveloper(game.getDeveloper());
        gameDTO.setDescription(game.getDescription());
        gameDTO.convertRupiah(game.getPrice());
        gameDTO.setSteamLink(game.getSteamLink());
        Date releaseDate = game.getReleaseDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(releaseDate);
        gameDTO.setDateString(formattedDate);
        gameDTO.setImage(Base64.getEncoder().encodeToString(game.getGameImage()));
        List<Rating> ratings = ratingService.getRating(game.getId());
        if(ratings.isEmpty()) {
            gameDTO.setRating(0.0);
            gameDTO.setRatingSum(0);
        } else {
            Double rating = 0.0;
            for (Rating rate : ratings) {
                rating += rate.getRatingValue();
            }
            gameDTO.setRating(rating/ratings.size());
            gameDTO.setRatingSum(ratings.size());
        }
        return gameDTO;
    }

    public List<GameDTO> getOtherGames(int id){

        List<GameDTO> gameResponse = new ArrayList<>();

        List<Game> games = gameRepository.findAll();
        Set<Game> hybridFilter = hybridFilterService.hybridFilter(id);

        games.removeAll(hybridFilter);
        return getGameDTOS(gameResponse, games);
    }

    public List<GameDTO> gamesByRatings(List<Rating> ratings){
        List<GameDTO> gameResponse = new ArrayList<>();
        List<Game> games = new ArrayList<>();
        ratings.forEach(it -> {
            games.add(it.getGame());
        });

        return getGameDTOS(gameResponse, games);
    }

    public List<GameDTO> searchByName(String name){
        List<GameDTO> gameResponse = new ArrayList<>();

        List<Game> games = gameRepository.searchByTitleLike(name);
        return getGameDTOS(gameResponse, games);
    }

    private List<GameDTO> getGameDTOS(List<GameDTO> gameResponse, List<Game> games) {
        games.forEach(it -> {

            GameDTO gameDTO = new GameDTO();

            gameDTO.setGenres(it.getGenres().stream().map(Genre::getName).collect(Collectors.joining(", ")));
            gameDTO.setId(it.getId());
            gameDTO.setTitle(it.getTitle());
            gameDTO.setDeveloper(it.getDeveloper());
            gameDTO.setDescription(it.getDescription());
            gameDTO.setReleaseDate(it.getReleaseDate());
            gameDTO.setImage(Base64.getEncoder().encodeToString(it.getGameImage()));
            gameDTO.setSteamLink(it.getSteamLink());
            gameDTO.convertRupiah(it.getPrice());
            List<Rating> ratings = ratingService.getRating(it.getId());
            if(ratings.isEmpty()) {
                gameDTO.setRating(0.0);
                gameDTO.setRatingSum(0);
            } else {
                Double rating = 0.0;
                for (Rating rate : ratings) {
                    rating += rate.getRatingValue();
                }
                gameDTO.setRating(rating/ratings.size());
                gameDTO.setRatingSum(ratings.size());
            }
            gameResponse.add(gameDTO);
        });

        return gameResponse;
    }

    public Game save(Game game){
        return gameRepository.save(game);
    }

    public List<GameDTO> hybridFilter(int id){
        List<GameDTO> gameResponse = new ArrayList<>();

        Set<Game> games = hybridFilterService.hybridFilter(id);
        games.forEach(it -> {
            GameDTO gameDTO = new GameDTO();

            gameDTO.setGenres(it.getGenres().stream().map(Genre::getName).collect(Collectors.joining(", ")));
            gameDTO.setId(it.getId());
            gameDTO.setTitle(it.getTitle());
            gameDTO.setDeveloper(it.getDeveloper());
            gameDTO.setDescription(it.getDescription());
            gameDTO.setReleaseDate(it.getReleaseDate());
            gameDTO.setImage(Base64.getEncoder().encodeToString(it.getGameImage()));
            gameDTO.setSteamLink(it.getSteamLink());
            gameDTO.convertRupiah(it.getPrice());
            List<Rating> ratings = ratingService.getRating(it.getId());
            if(ratings.isEmpty()) {
                gameDTO.setRating(0.0);
                gameDTO.setRatingSum(0);
            } else {
                Double rating = 0.0;
                for (Rating rate : ratings) {
                    rating += rate.getRatingValue();
                }
                gameDTO.setRating(rating/ratings.size());
                gameDTO.setRatingSum(ratings.size());
            }
            gameResponse.add(gameDTO);
        });
        return gameResponse;
    }

    private String convertRupiah(double price){
        return new DecimalFormat("#,###").format(price);
    }


}
