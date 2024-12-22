package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.service.filter.HybridFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
public class GameFilterController {

    @Autowired
    private HybridFilterService hybridFilterService;

    @GetMapping("/filterGames")
    public String filterGames(@RequestParam("userId") Integer userId, Model model) {
        Set<Game> recommendedGames = hybridFilterService.hybridFilter(userId);
        model.addAttribute("recommendedGames", recommendedGames);
        return "filteredGames"; // Ganti dengan nama file Thymeleaf Anda
    }
}
