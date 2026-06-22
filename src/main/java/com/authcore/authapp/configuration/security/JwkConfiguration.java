package com.authcore.authapp.configuration.security;

import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.repository.AppUserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class JwkConfiguration {

    private final AppUserRepository appUserRepository;

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKeyPair() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication authentication = context.getPrincipal();

            if (context.getTokenType().getValue().equals("id_token")) {
                context.getClaims().claim("token_type", "id_token");
            }

            if (context.getTokenType().getValue().equals("access_token")) {
                AppUser appUser = resolveAppUser(authentication);

                Collection<? extends GrantedAuthority> resolvedAuthorities = appUser != null ? appUser.getAuthorities()
                        : authentication.getAuthorities();

                Set<String> allAuthorities = resolvedAuthorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                Set<String> roles = allAuthorities.stream()
                        .filter(a -> a.startsWith("ROLE_"))
                        .collect(Collectors.toSet());
                Set<String> permissions = allAuthorities.stream()
                        .filter(a -> !a.startsWith("ROLE_"))
                        .collect(Collectors.toSet());

                String userId = appUser != null ? appUser.getId().toString() : null;
                context.getClaims()
                        .claim("token_type", "access_token")
                        .claim("authorities", allAuthorities)
                        .claim("roles", roles)
                        .claim("permissions", permissions)
                        .claim("user", authentication.getName())
                        .claim("user_id", userId);
            }
        };
    }

    private AppUser resolveAppUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUser appUser) {
            return appUser;
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return appUserRepository.findByEmail(email).orElse(null);
            }
        }
        return null;
    }
}
