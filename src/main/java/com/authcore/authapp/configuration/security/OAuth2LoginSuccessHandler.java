package com.authcore.authapp.configuration.security;

import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.services.user.AppUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AppUserService appUserService;

    public OAuth2LoginSuccessHandler(AppUserService appUserService) {
        setDefaultTargetUrl("/success");
        setAlwaysUseDefaultTargetUrl(false);
        this.appUserService = appUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        if (authentication instanceof OAuth2AuthenticationToken oAuth2Token
                && oAuth2Token.getPrincipal() instanceof OAuth2User oAuth2User) {

            String email = oAuth2User.getAttribute("email");
            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");

            log.info("OAuth2 login success, creating/finding user: {}", email);

            if (email != null) {
                try {
                    AppUser appUser = appUserService.findOrCreateOAuth2User(email, givenName, familyName);
                    log.info("User {} saved to database, id={}", email, appUser.getId());
                } catch (Exception e) {
                    log.error("Failed to save OAuth2 user {}: {}", email, e.getMessage(), e);
                }
            }
        }

        HttpSession session = request.getSession(false);
        SavedRequest savedRequest = session != null
                ? (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST")
                : null;

        boolean isOAuth2AuthFlow = savedRequest != null
                && savedRequest.getRedirectUrl() != null
                && savedRequest.getRedirectUrl().contains("/oauth2/authorize");

        if (!isOAuth2AuthFlow) {
            if (session != null) {
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            }
            setDefaultTargetUrl("/success");
            setAlwaysUseDefaultTargetUrl(true);
        } else {
            setDefaultTargetUrl("/success");
            setAlwaysUseDefaultTargetUrl(false);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
