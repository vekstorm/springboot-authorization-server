
package com.authcore.authapp.dto.client;

import lombok.Data;
import java.util.Set;

@Data
public class ClientUpdateDto {

    private String id;

    private String clientId;

    private String clientSecret;

    private String clientName;

    private Set<String> authenticationMethods;

    private Set<String> authorizationGrantTypes;

    private Set<String> redirectUris;

    private Set<String> postLogoutRedirectUris;

    private Set<String> scopes;

    private boolean requireProofKey;

}
