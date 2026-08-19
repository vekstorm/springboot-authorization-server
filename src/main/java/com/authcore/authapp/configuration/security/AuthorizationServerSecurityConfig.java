package com.authcore.authapp.configuration.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class AuthorizationServerSecurityConfig {

        @Value("${base.url}")
        private String baseUrl;

        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
        private final FormLoginSuccessHandler formLoginSuccessHandler;

        @Bean
        public DynamicLogoutSuccessHandler dynamicLogoutSuccessHandler() {
                return new DynamicLogoutSuccessHandler();
        }

        @Bean
        @Order(0)
        public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher(
                                                "/swagger-ui",
                                                "/swagger-ui/",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/api-docs",
                                                "/api-docs/",
                                                "/api-docs/**",
                                                "/api-docs.yaml",
                                                "/api-docs/swagger-config",
                                                "/.well-known/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().permitAll())
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .oauth2Login(AbstractHttpConfigurer::disable)
                                .oauth2ResourceServer(AbstractHttpConfigurer::disable);

                return http.build();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
                http
                                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
.with(authorizationServerConfigurer,
                                                (authorizationServer) -> authorizationServer
                                                                .oidc(Customizer.withDefaults()))
                                 .authorizeHttpRequests((authorize) -> authorize
                                                 .requestMatchers("/oauth2/jwks").permitAll()
                                                 .anyRequest().authenticated())
                                .exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/v1/client/**", "/api/v1/user/**", "/api/v1/role/**",
                                                "/api/v1/permission/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                                .csrf(AbstractHttpConfigurer::disable);
                return http.build();
        }

        @Bean
        @Order(3)
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/login",
                                                                "/register",
                                                                "/exit",
                                                                "/error",
                                                                "/main.css",
                                                                "/assets/**",
                                                                "/.well-known/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(formLoginSuccessHandler)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .successHandler(oAuth2LoginSuccessHandler))
                                .logout(logout -> logout
                                                .logoutSuccessHandler(dynamicLogoutSuccessHandler())
                                                .permitAll());
                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");
                grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder().issuer(baseUrl).build();
        }

}
