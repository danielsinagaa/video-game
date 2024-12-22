package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.service.GameService;
import com.hybrid.filter.video_game.service.RatingService;
import com.hybrid.filter.video_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProfileController {
    @Autowired
    UserService userService;

    @Autowired
    RatingService ratingService;

    @Autowired
    GameService gameService;

    @GetMapping("/profile")
    public String profile(@RequestParam("username") String username, @RequestParam("email") String email, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        User user = userService.getUserByUsername(username);
        List<Rating> rating = ratingService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("games", gameService.gamesByRatings(rating));

        return "profile";
    }
}
