package com.authcore.authapp.dto.client.mapper;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

import com.authcore.authapp.dto.client.ClientResponseDto;
import com.authcore.authapp.models.Client;

import java.util.HashSet;
import java.util.Set;

@Component
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

    public Page<ClientResponseDto> toClientResponseDtoPage(Page<Client> clients) {
        return clients.map(client -> {
            ClientResponseDto dto = new ClientResponseDto();
            dto.setClientId(client.getClientId());
            dto.setClientName(client.getClientName());
            dto.setAuthenticationMethods(client.getAuthenticationMethods().stream()
                    .map(ClientAuthenticationMethod::getValue).collect(java.util.stream.Collectors.toSet()));
            dto.setAuthorizationGrantTypes(client.getAuthorizationGrantTypes().stream()
                    .map(AuthorizationGrantType::getValue).collect(java.util.stream.Collectors.toSet()));
            dto.setRedirectUris(client.getRedirectUris());
            dto.setPostLogoutRedirectUris(client.getPostLogoutRedirectUris());
            dto.setScopes(client.getScopes());
            dto.setRequireProofKey(client.isRequireProofKey());
            return dto;
        });
    }
}
