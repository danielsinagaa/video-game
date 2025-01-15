package com.hybrid.filter.video_game.controller;

import com.hybrid.filter.video_game.model.entity.User;
import com.hybrid.filter.video_game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";  // Halaman register.html
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam("username") String username,
                                      @RequestParam("email") String email,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword){
        if (!password.equals(confirmPassword)) {
            return "registerErrorPassword";
        }
        User user = userService.getUser(email);
        if (user != null) {
            return "registerError";
        }

        user = userService.getUserByUsername(username);
        if (user != null) {
            return "registerError";
        }

        user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        userService.save(user);
        return "redirect:/successAccount?username=" + username + "&email=" + email;
    }

    @GetMapping("/successAccount")
    public String showSuccessAccount(){
        return "successAccount";
    }
}
