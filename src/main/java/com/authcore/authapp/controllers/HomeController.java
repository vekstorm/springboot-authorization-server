package com.authcore.authapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    @GetMapping("/")
    @PreAuthorize("hasAuthority('client:read')")
    public String index(Authentication authentication, Model model) {
        var authorities = authentication.getAuthorities();
        authorities = authorities.stream().filter(auth -> auth.getAuthority().indexOf(":") > 0)
                .collect(Collectors.toList());
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authorities);
        }
        return "home";
    }

}