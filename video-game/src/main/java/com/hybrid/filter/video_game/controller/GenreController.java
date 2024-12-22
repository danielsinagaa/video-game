package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.service.GameService;
import com.hybrid.filter.video_game.service.GenreService;
import com.hybrid.filter.video_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Controller
public class GenreController {
    @Autowired
    private GenreService genreService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @GetMapping("/genre/add")
    public String addGenre(@RequestParam("username") String username,
                           @RequestParam("email") String email, Model model) {

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "genreAdd";
    }

    @GetMapping("/genre/game")
    public String genre(Model model,@RequestParam("id")int id,
                        @RequestParam("username") String username,
                        @RequestParam("email") String email,
                        @RequestParam(value = "search", required = false) String searchQuery) {

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
        model.addAttribute("role", user.getRole());

        List<GameDTO> searchResults = gameService.searchByName(searchQuery); // Mendapatkan hasil pencarian game
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);

        List<GameDTO> genreSearch = gameService.getGamesByGenreId(id);
        model.addAttribute("genreSearch", genreSearch);

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);
        return "gameGenre";
    }

    @PostMapping("/genre/add")
    public String saveGenre(@RequestParam("username") String username,
                            @RequestParam("email") String email,
                            @RequestParam("name") String name,
                            @RequestParam("genreImage") MultipartFile genreImage,
                            Model model) {
        // Validasi data input
        if (name.isEmpty() || genreImage.isEmpty()) {
            model.addAttribute("error", "Both fields are required.");
            return "genreAdd";
        }

        try {
            // Simpan genre menggunakan genreService
            genreService.saveGenre(name, genreImage);
            model.addAttribute("success", "Genre added successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
        }
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "genreAddSuccess";
    }
}
