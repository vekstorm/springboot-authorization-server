package com.authcore.authapp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Entity(name = "client")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private Instant clientIdIssuedAt;

    private String clientSecret;

    private Instant clientSecretExpiresAt;

    private String clientName;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ClientAuthenticationMethod> authenticationMethods;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<AuthorizationGrantType> authorizationGrantTypes;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> redirectUris;

    @Column(length = 1000)
    private Set<String> postLogoutRedirectUris;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> scopes;

    private boolean requireProofKey;

    @Column(length = 2000)
    private String clientSettings;

    @Column(length = 2000)
    private String tokenSettings;

    public static RegisteredClient toRegisteredClient(Client client) {
        if (client == null) {
            return null;
        }

        RegisteredClient.Builder builder = RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientIdIssuedAt(client.getClientIdIssuedAt() != null ? client.getClientIdIssuedAt() : Instant.now())
                .clientSecret(client.getClientSecret())
                .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                .clientName(client.getClientName())
                .clientAuthenticationMethods(methods -> {
                    if (client.getAuthenticationMethods() != null) {
                        methods.addAll(client.getAuthenticationMethods());
                    }
                })
                .authorizationGrantTypes(grantTypes -> {
                    if (client.getAuthorizationGrantTypes() != null) {
                        grantTypes.addAll(client.getAuthorizationGrantTypes());
                    }
                })
                .redirectUris(uris -> {
                    if (client.getRedirectUris() != null) {
                        uris.addAll(client.getRedirectUris());
                    }
                })
                .postLogoutRedirectUris(uris -> {
                    if (client.getPostLogoutRedirectUris() != null) {
                        uris.addAll(client.getPostLogoutRedirectUris());
                    }
                })
                .scopes(scopes -> {
                    if (client.getScopes() != null) {
                        scopes.addAll(client.getScopes());
                    }
                })
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(client.isRequireProofKey())
                        .build())
                .tokenSettings(TokenSettings.builder().build());

        return builder.build();
    }
}
