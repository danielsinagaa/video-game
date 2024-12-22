package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    LoginService loginService;

    @GetMapping("")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/login")
    public String loginFromRegister(Model model) {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(){
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, Model model) {
        User user = loginService.login(username, password);

        // Jika user null, berarti login gagal
        if (user == null) {  // Pass an error to show the alert
            return "loginFailed";  // Navigate to the loginFailed.html page
        }

        // Jika login berhasil, arahkan ke home page
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "redirect:/home?username=" + user.getUsername() + "&email=" + user.getEmail();  // Redirect ke /home dengan parameter
    }
}
