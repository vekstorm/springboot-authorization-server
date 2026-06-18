package com.authcore.authapp.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI(
                        @Value("${base.url}") String host,
                        @Value("${server.port}") String port) {
                String issuer = host + ":" + port;

                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes("oauth2", new SecurityScheme()
                                                                .type(SecurityScheme.Type.OAUTH2)
                                                                .flows(new OAuthFlows()
                                                                                .authorizationCode(new OAuthFlow()
                                                                                                .authorizationUrl(issuer
                                                                                                                + "/oauth2/authorize")
                                                                                                .tokenUrl(issuer + "/oauth2/token")
                                                                                                .scopes(new Scopes()
                                                                                                                .addString("openid",
                                                                                                                                "OpenID")
                                                                                                                .addString("profile",
                                                                                                                                "Profile"))))))
                                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
        }
}
