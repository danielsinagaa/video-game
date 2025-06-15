package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.entity.*;
import com.hybrid.filter.video_game.repository.GameGenreRepository;
import com.hybrid.filter.video_game.service.GameService;
import com.hybrid.filter.video_game.service.GenreService;
import com.hybrid.filter.video_game.service.RatingService;
import com.hybrid.filter.video_game.service.UserService;
import com.hybrid.filter.video_game.service.filter.genre.HybridFilterGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class GameController {

    @Autowired
    private UserService userService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private GameService gameService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private GameGenreRepository gameGenreRepository;

    @Autowired
    private HybridFilterGenreService hybridFilterGenreService;

    @GetMapping("/game/delete")
    public String deleteGame(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("gameId") int gameId, Model model) {

        // Menghapus game berdasarkan gameId
        gameService.removeGame(gameId);

        // Menambahkan data username dan email ke model untuk digunakan di halaman home
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        // Redirect ke halaman home setelah penghapusan
        return "redirect:/home?username=" + username + "&email=" + email;
    }

    @GetMapping("/game/edit")
    public String editGame(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("gameId") int gameId, Model model) {
        // Fetch the game based on the ID
        GameDTO game = gameService.getGameById(gameId);

        // Add the game and user details to the model
        model.addAttribute("game", game);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("genres", genreService.getAllGenresDTO());

        return "gameEdit";  // Return to the edit game page (editGame.html)
    }

    @PostMapping("/game/edit")
    public String updateGame(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("id") int id,
                             @RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("releaseDate") String releaseDate,
                             @RequestParam("developer") String developer,
                             @RequestParam("steamLink") String steamLink,
                             @RequestParam("price") Double price,
                             @RequestParam(value = "genre1", required = true) Integer genreId1,
                             @RequestParam(value = "genre2", required = false) Integer genreId2,
                             @RequestParam(value = "genre3", required = false) Integer genreId3,
                             @RequestParam(value = "genre4", required = false) Integer genreId4,
                             @RequestParam("gameImage") MultipartFile gameImage,
                             Model model) {

        // Retrieve the existing game
        Game game = gameService.findGame(id);

        // Update the game fields
        game.setTitle(title);
        game.setDescription(description);
        game.setReleaseDate(Date.valueOf(releaseDate));  // Convert String to Date
        game.setDeveloper(developer);
        game.setSteamLink(steamLink);
        game.setPrice(price);

        // Handle image upload if there's a new one
        if (!gameImage.isEmpty()) {
            try {
                byte[] bytes = gameImage.getBytes();
                game.setGameImage(bytes);  // Save the new image
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Error uploading the image!");
                return "editGame";
            }
        }

        // Set genres
        List<Genre> selectedGenres = new ArrayList<>();
        if (genreId1 != null) selectedGenres.add(genreService.getGenreById(genreId1));
        if (genreId2 != null) selectedGenres.add(genreService.getGenreById(genreId2));
        if (genreId3 != null) selectedGenres.add(genreService.getGenreById(genreId3));
        if (genreId4 != null) selectedGenres.add(genreService.getGenreById(genreId4));

        game.setGenres(selectedGenres);

        // Save the updated game
        gameService.save(game);

        return "redirect:/game/editSuccess?username=" + username + "&email=" + email;
    }

    @GetMapping("/game")
    public String game(@RequestParam("username") String username,
                       @RequestParam("email") String email,
                       @RequestParam(value = "search", required = false) String searchQuery,
                       Model model) {

        List<GenreDTO> genres = genreService.getAllGenresDTO();
        for (GenreDTO genre : genres) {
            byte[] genreImage = genre.getGenreImage();
            String base64Image = null;
            if (genreImage != null) {
                base64Image = Base64.getEncoder().encodeToString(genreImage);
            }
            genre.setGenreImage64(base64Image);
        }

        // Menambahkan data genre dan user ke model
        model.addAttribute("genres", genres);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User();
            user.setId(0);
            user.setUsername("");
            user.setPassword("");
            user.setRole(false);
        }
        model.addAttribute("role", user.getRole());

        // Jika ada query pencarian

        List<GameDTO> searchResults = gameService.searchByName(searchQuery); // Mendapatkan hasil pencarian game
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);

        model.addAttribute("hybridGenre", hybridFilterGenreService.genreFilter(user.getId()));

        return "game";
    }

    @GetMapping("/game/add")
    public String gameAdd(@RequestParam("username") String username,
                          @RequestParam("email") String email, Model model) {


        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("genres", genreService.getAllGenresDTO());
        return "gameAdd";
    }

    @PostMapping("/game/add")
    public String addGame(@RequestParam("username") String username,
                          @RequestParam("email") String email,
                          @RequestParam("title") String title,  // Data dari form game
                          @RequestParam("description") String description,
                          @RequestParam("releaseDate") String releaseDate,  // Release date sebagai String
                          @RequestParam("developer") String developer,  // Developer name
                          @RequestParam(value = "genre1", required = true) Integer genreId1,  // ID genre pertama
                          @RequestParam(value = "genre2", required = false) Integer genreId2,  // ID genre kedua
                          @RequestParam(value = "genre3", required = false) Integer genreId3,  // ID genre ketiga
                          @RequestParam(value = "genre4", required = false) Integer genreId4,  // ID genre keempat
                          @RequestParam("gameImage") MultipartFile gameImage,  // File gambar
                          @RequestParam("steamLink") String steamLink,
                          @RequestParam("price") Double price,
                          Model model) {

        // Ambil genre berdasarkan ID
        List<Genre> selectedGenres = new ArrayList<>();

        // Menambahkan genre ke list jika tidak null
        if (genreId1 != null) {
            Genre genre1 = genreService.getGenreById(genreId1);
            selectedGenres.add(genre1);
        }
        if (genreId2 != null) {
            Genre genre2 = genreService.getGenreById(genreId2);
            selectedGenres.add(genre2);
        }
        if (genreId3 != null) {
            Genre genre3 = genreService.getGenreById(genreId3);
            selectedGenres.add(genre3);
        }
        if (genreId4 != null) {
            Genre genre4 = genreService.getGenreById(genreId4);
            selectedGenres.add(genre4);
        }

        // Buat objek game baru dan set data dari form
        Game game = new Game();
        game.setTitle(title);
        game.setDescription(description);
        game.setReleaseDate(Date.valueOf(releaseDate));  // Convert String ke Date
        game.setDeveloper(developer);
        game.setSteamLink(steamLink);
        game.setPrice(price);

        // Set genre ke game
        game.setGenres(selectedGenres);

        // Set game image jika ada
        if (!gameImage.isEmpty()) {
            try {
                byte[] bytes = gameImage.getBytes();
                game.setGameImage(bytes);  // Menyimpan gambar sebagai byte array
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Error uploading the image!");
                return "gameAdd"; // Mengembalikan ke halaman form jika ada error
            }
        }

        // Simpan game menggunakan GameService atau repository
        Game saveGame = gameService.save(game);
        for (Genre it : selectedGenres) {
            GameGenre gameGenre = new GameGenre();
            gameGenre.setGame(saveGame);
            gameGenre.setGenre(it);
            gameGenreRepository.save(gameGenre);
        }

        // Kembali ke halaman daftar game atau halaman sukses
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "redirect:/game/addSuccess?username=" + username + "&email=" + email;  // Redirect ke halaman game add success
    }


    @GetMapping("/game/addSuccess")
    public String gameAddSuccess(@RequestParam("username") String username,
                                 @RequestParam("email") String email,
                                 Model model) {

        model.addAttribute("username", username);
        model.addAttribute("email", email);

        return "gameAddSuccess";
    }


    @GetMapping("/game/editSuccess")
    public String gameEditSuccess(@RequestParam("username") String username,
                                 @RequestParam("email") String email,
                                 Model model) {

        model.addAttribute("username", username);
        model.addAttribute("email", email);

        return "gameEditSuccess";
    }

    @GetMapping("/game/detail")
    public String gameDetail(@RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("gameId") int gameId,
                             Model model){


        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("gameId", gameId);
        GameDTO gameDTO = gameService.getGameById(gameId);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User();
            user.setId(0);
            user.setUsername("");
            user.setPassword("");
            user.setRole(false);
        }
        model.addAttribute("gameDTO", gameDTO);

        Rating rating = ratingService.findByUserIdAndGameId(user.getId(), gameId);
        model.addAttribute("rating", rating);

        return "gameDetail";
    }

    @PostMapping("/game/rate")
    public String gameRate(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("gameId") int gameId,
                           @RequestParam("ratingValue") int ratingValue,
                           @RequestParam(value = "rating", required = false) Rating rating,
                           @RequestParam(value = "search", required = false) String searchQuery,
                           Model model){

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("gameId", gameId);
        Game game = gameService.findGame(gameId);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User();
            user.setId(0);
            user.setUsername("");
            user.setPassword("");
            user.setRole(false);
        }

        Rating existingRating = ratingService.findByUserIdAndGameId(user.getId(), gameId);
        if (existingRating != null) {
            existingRating.setRatingValue(ratingValue);
            ratingService.save(existingRating); // Update rating yang sudah ada
        } else {
            Rating newRating = new Rating();
            if (rating != null){
                if (rating.getId() != null) newRating.setId(rating.getId());
            }
            newRating.setUser(user);
            newRating.setGame(game);
            newRating.setRatingValue(ratingValue);
            ratingService.save(newRating); // Simpan rating baru jika belum ada
        }

        List<GenreDTO> genres = genreService.getAllGenresDTO();
        for (GenreDTO genre : genres) {
            byte[] genreImage = genre.getGenreImage();
            String base64Image = null;
            if (genreImage != null) {
                base64Image = Base64.getEncoder().encodeToString(genreImage);
            }
            genre.setGenreImage64(base64Image);
        }

        // Menambahkan data genre dan user ke model
        model.addAttribute("genres", genres);
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        model.addAttribute("role", user.getRole());

        // Jika ada query pencarian

        List<GameDTO> searchResults = gameService.searchByName(searchQuery); // Mendapatkan hasil pencarian game
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);

        return "game";
    }

}
