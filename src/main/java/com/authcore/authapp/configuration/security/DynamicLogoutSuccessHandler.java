package com.authcore.authapp.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

public class DynamicLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:4200",
            "http://192.168.1.41:4200",
            "http://localhost:4201",
            "http://192.168.1.41:4201",
            "http://localhost:8081",
            "http://192.168.1.41:8081",
            "https://miapp.com"); // TODO -> Whitelist administrable

    private static final String DEFAULT_TARGET = "/login?logout";

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        new SecurityContextLogoutHandler().logout(request, response, authentication);
        new CookieClearingLogoutHandler("JSESSIONID").logout(request, response, authentication);

        String origin = request.getHeader("Origin");
        if (origin == null) {
            String referer = request.getHeader("Referer"); // Obtenemos la ip a través de las cabeceras de la petición
            if (referer != null) {
                try {
                    URI uri = URI.create(referer);
                    origin = uri.getScheme() + "://" + uri.getAuthority();
                } catch (Exception ignored) {
                }
            }
        }

        String target = (origin != null && ALLOWED_ORIGINS.contains(origin))
                ? origin + "/"
                : DEFAULT_TARGET;

        response.sendRedirect(target);
    }
}
