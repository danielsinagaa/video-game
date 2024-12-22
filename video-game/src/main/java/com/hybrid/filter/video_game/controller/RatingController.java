package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.RatingDTO;
import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.Rating;
import com.hybrid.filter.video_game.model.model.RatingForm;
import com.hybrid.filter.video_game.repository.GameRepository;
import com.hybrid.filter.video_game.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RatingController {

    private final RatingService ratingService;
    private final GameRepository gameRepository;

    @Autowired
    public RatingController(RatingService ratingService, GameRepository gameRepository) {
        this.ratingService = ratingService;
        this.gameRepository = gameRepository;
    }

    @GetMapping("/rate")
    public String showRatingForm(Model model) {
        List<Game> games = gameRepository.findAll(); // Mengambil semua game
        model.addAttribute("games", games); // Menambahkan daftar game ke model
        model.addAttribute("ratingDTO", new RatingDTO());
        return "rate"; // Mengarah ke template Thymeleaf rate.html
    }

    @PostMapping("/rate")
    public String submitRating(RatingDTO ratingDTO, Model model) {
        String message = ratingService.addRating(ratingDTO);
        model.addAttribute("message", message);
        return "result"; // Mengarah ke template Thymeleaf result.html
    }
}
