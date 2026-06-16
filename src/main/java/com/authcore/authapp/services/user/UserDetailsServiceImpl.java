package com.authcore.authapp.services.user;

import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.repository.AppUserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Set<String> CLIENT_PERMISSIONS = Set.of("client:read", "client:write", "client:delete");

    private static final String SPRING_SECURITY_SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        boolean hasClientPermission = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> CLIENT_PERMISSIONS.contains(perm.getName()));

        if (!hasClientPermission) {
            if (!isOAuth2AuthorizationFlow()) {
                log.warn("Login denied for user {}: missing client management permissions", email);
                throw new UsernameNotFoundException("Invalid email or password");
            }
            log.info("User {} authenticated via OAuth2 authorization flow without client permissions", email);
        }

        return user;
    }

    private boolean isOAuth2AuthorizationFlow() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpSession session = servletRequestAttributes.getRequest().getSession(false);
                if (session != null) {
                    Object savedRequestObj = session.getAttribute(SPRING_SECURITY_SAVED_REQUEST);
                    if (savedRequestObj instanceof SavedRequest savedRequest) {
                        String redirectUrl = savedRequest.getRedirectUrl();
                        return redirectUrl != null && redirectUrl.contains("/oauth2/authorize");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error checking OAuth2 authorization flow", e);
        }
        return false;
    }

}
