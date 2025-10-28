package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.dto.GameDTO;
import com.hybrid.filter.video_game.model.dto.GenreDTO;
import com.hybrid.filter.video_game.model.dto.GenreFilterDTO;
import com.hybrid.filter.video_game.model.dto.TopGenreFilterDTO;
import com.hybrid.filter.video_game.model.entity.Game;
import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.repository.UserRepository;
import com.hybrid.filter.video_game.service.GameService;
import com.hybrid.filter.video_game.service.GenreService;
import com.hybrid.filter.video_game.service.GuestPreferenceService;
import com.hybrid.filter.video_game.service.UserService;
import com.hybrid.filter.video_game.service.filter.genre.HybridFilterGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private GuestPreferenceService  guestPreferenceService;

    @Autowired
    private HybridFilterGenreService hybridFilterGenreService;

    @GetMapping("/home-guest")
    public String homeGuest(Model model,
                       @RequestParam(value = "search", required = false) String searchQuery) {
        List<TopGenreFilterDTO> genres = genreService.getTop5GamesByGenre();
        List<GameDTO> freeGames = gameService.getFreeGames();
        List<Integer> years = gameService.getReleaseYears(); // Ambil daftar tahun rilis

        model.addAttribute("genres", genres);
        model.addAttribute("freeGames", freeGames);
        model.addAttribute("years", years); // Tambahkan tahun ke model

        User user = new User();
        user.setId(0);
        user.setUsername("");
        user.setPassword("");
        user.setRole(false);

        model.addAttribute("role", user.getRole());

        List<GameDTO> searchResults = gameService.searchByName(searchQuery); // Mendapatkan hasil pencarian game
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);

        model.addAttribute("hybridGenre", hybridFilterGenreService.genreFilter(user.getId()));

        return "home";
    }

    @GetMapping("/home")
    public String home(@RequestParam("username") String username,
                       @RequestParam("email") String email,
                       @RequestParam(value = "year", required = false) Integer year,
                       Model model,
                       @RequestParam(value = "search", required = false) String searchQuery) {
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

        // 🔹 Cek apakah user sudah punya preferensi genre
        boolean hasPreference = guestPreferenceService.existsByUserId(user.getId());
        if (!hasPreference) {
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("genres", genreService.getAllGenresDTO());
            return "genrePreference";
        }

        List<TopGenreFilterDTO> genres = genreService.getTop5GamesByGenre();
        List<GameDTO> freeGames = gameService.getFreeGames();
        List<Integer> years = gameService.getReleaseYears(); // Ambil daftar tahun rilis

        model.addAttribute("genres", genres);
        model.addAttribute("freeGames", freeGames);
        model.addAttribute("years", years); // Tambahkan tahun ke model

        // Jika parameter tahun disediakan, cari game berdasarkan tahun
        if (year != null) {
            List<GameDTO> gamesByYear = gameService.findGamesByYear(year);
            model.addAttribute("gamesByYear", gamesByYear);
            model.addAttribute("selectedYear", year); // Menandai tahun yang dipilih
        }

        model.addAttribute("role", user.getRole());

        List<GameDTO> searchResults = gameService.searchByName(searchQuery); // Mendapatkan hasil pencarian game
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchResults", searchResults);

        List<GameDTO> hybridFilter = gameService.hybridFilter(user.getId());
        model.addAttribute("hybridFilter", hybridFilter);

        List<GameDTO> otherGames = gameService.getOtherGames(user.getId());
        model.addAttribute("otherGames", otherGames);

        model.addAttribute("hybridGenre", hybridFilterGenreService.genreFilter(user.getId()));

        return "home";
    }

    @PostMapping("/preference/save")
    public String savePreference(@RequestParam("selectedGenres") List<Integer> selectedGenres,
                                 @RequestParam("username") String username, Model model) {

        // Ubah list jadi string "2,5,9"
        String genreIds = selectedGenres.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Integer userId = 0;
        User user = new User();
        if (username != null && !username.isEmpty()) {
            user = userService.getUserByUsername(username);
            if (user != null) {
                userId = user.getId();
            }
        }

        guestPreferenceService.saveGuestPreference(userId, null, genreIds);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        return "redirect:/home?username=" + username + "&email=" + user.getEmail();
    }


}
