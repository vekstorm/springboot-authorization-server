package com.authcore.authapp.configuration.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class FormLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws ServletException, IOException {
                boolean hasClientAccess = authentication.getAuthorities().stream()
                                .anyMatch(a -> "client:read".equals(a.getAuthority()));

                log.info("Form login successful for user: {}, hasClientAccess: {}",
                                authentication.getName(), hasClientAccess);

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
                        setDefaultTargetUrl(hasClientAccess ? "/" : "/success");
                        setAlwaysUseDefaultTargetUrl(true);
                } else {
                        setDefaultTargetUrl(hasClientAccess ? "/" : "/success");
                        setAlwaysUseDefaultTargetUrl(false);
                }

                super.onAuthenticationSuccess(request, response, authentication);
        }
}
