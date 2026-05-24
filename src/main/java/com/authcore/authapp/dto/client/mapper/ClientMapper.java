package com.authcore.authapp.dto.client.mapper;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.HashSet;
import java.util.Set;

public class ClientMapper {

    public static Set<AuthorizationGrantType> getAuthorizationGrantTypesFromNamesList(Set<String> namesList) {
        Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>();
        if (namesList != null) {
            for (String name : namesList) {
                authorizationGrantTypes.add(new AuthorizationGrantType(name));
            }
        }
        return authorizationGrantTypes;
    }

    public static Set<ClientAuthenticationMethod> getClientAuthenticationMethodsFromNamesList(Set<String> namesList) {
        Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>();
        if (namesList != null) {
            for (String name : namesList) {
                clientAuthenticationMethods.add(new ClientAuthenticationMethod(name));
            }
        }
        return clientAuthenticationMethods;
    }
}
