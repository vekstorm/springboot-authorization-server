package com.authcore.authapp.dto.client;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponseDto {

    private String clientId;

    private String clientName;

    private Set<String> authenticationMethods;

    private Set<String> authorizationGrantTypes;

    private Set<String> redirectUris;

    private Set<String> postLogoutRedirectUris;

    private Set<String> scopes;

    private boolean requireProofKey;

}
