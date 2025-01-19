package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.dto.GenreFilterDTO;
import com.hybrid.filter.video_game.model.dto.TopGenreFilterDTO;
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

    @GetMapping("/home-guest")
    public String homeGuest(Model model) {

        List<TopGenreFilterDTO> genres = genreService.getTop5GamesByGenre();

        // Menambahkan data genre dan user ke model
        model.addAttribute("genres", genres);
        User user = new User();
        user.setId(0);
        user.setUsername("");
        user.setPassword("");
        user.setRole(false);

        model.addAttribute("role", user.getRole());


        return "home";
    }

    @GetMapping("/home")
    public String home(@RequestParam("username") String username, @RequestParam("email") String email, Model model) {
        // Menambahkan username dan email ke model untuk diteruskan ke halaman
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        List<TopGenreFilterDTO> genres = genreService.getTop5GamesByGenre();

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


        return "home";
    }
}
