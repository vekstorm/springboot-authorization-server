package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.services.user.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping("/success")
    public String oauth2Success(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {

            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                AppUser appUser = appUserService.findByEmail(oAuth2User.getAttribute("email"));
                model.addAttribute("username", appUser.getUsername());
                model.addAttribute("email", appUser.getEmail());
                model.addAttribute("name", appUser.getName());
                model.addAttribute("surname", appUser.getSurname1());
                model.addAttribute("permissions", appUser.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct()
                        .toList());
                model.addAttribute("isGoogle", true);
            } else if (authentication.getPrincipal() instanceof AppUser user) {
                model.addAttribute("username", user.getUsername());
                model.addAttribute("email", user.getEmail());
                model.addAttribute("name", user.getName());
                model.addAttribute("surname", user.getSurname1());
                model.addAttribute("permissions", user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct()
                        .toList());
                model.addAttribute("isGoogle", false);
            }
        }
        return "success";
    }

}