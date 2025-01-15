package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.dto.GenreFilterDTO;
import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.service.GameService;
import com.hybrid.filter.video_game.service.GenreService;
import com.hybrid.filter.video_game.service.UserService;
import com.hybrid.filter.video_game.service.filter.genre.HybridFilterGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private HybridFilterGenreService hybridFilterGenreService;

    @GetMapping("/home")
    public String home(@RequestParam("username") String username, @RequestParam("email") String email, Model model) {
        // Menambahkan username dan email ke model untuk diteruskan ke halaman
        model.addAttribute("username", username);
        model.addAttribute("email", email);

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

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);

        List<GenreFilterDTO> hybridGenre = hybridFilterGenreService.genreFilter(user.getId());
        model.addAttribute("hybridGenre", hybridGenre);

        return "home";
    }
}
