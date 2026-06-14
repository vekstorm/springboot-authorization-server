package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.services.user.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AppUserService appUserService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String mode, Model model) {
        if ("register".equals(mode)) {
            model.addAttribute("regMode", "register");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("regMode", "register");
        return "login";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("regError", "Passwords do not match");
            model.addAttribute("regMode", "register");
            model.addAttribute("regUsername", username);
            model.addAttribute("regEmail", email);
            return "login";
        }

        try {
            AppUserCreateDto dto = new AppUserCreateDto();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setRoles(List.of("ROLE_USER"));
            appUserService.createUser(dto);
            return "redirect:/login?registered";
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("usuario ya existe")) {
                message = "Username or email already in use";
            } else if (message != null && message.contains("duplicate")) {
                message = "Username or email already in use";
            } else if (message != null && message.contains("violates not-null")) {
                message = "Please fill in all required fields";
            } else {
                message = "Registration failed. Please try again.";
            }
            model.addAttribute("regError", message);
            model.addAttribute("regMode", "register");
            model.addAttribute("regUsername", username);
            model.addAttribute("regEmail", email);
            return "login";
        }
    }

}