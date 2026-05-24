package com.authcore.authapp.services.client;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.dto.client.mapper.ClientMapper;
import com.authcore.authapp.models.Client;
import com.authcore.authapp.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Service
public class ClientServiceImpl implements ClientService, RegisteredClientRepository {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    private Client clientFromDto(ClientCreateDto clientDto) {
        return Client.builder()
                .clientId(clientDto.getClientId())
                .clientSecret(passwordEncoder.encode(clientDto.getClientSecret()))
                .clientName(clientDto.getClientName())
                .clientIdIssuedAt(Instant.now())
                .authorizationGrantTypes(ClientMapper.getAuthorizationGrantTypesFromNamesList(clientDto.getAuthorizationGrantTypes()))
                .authenticationMethods(ClientMapper.getClientAuthenticationMethodsFromNamesList(clientDto.getAuthenticationMethods()))
                .redirectUris(clientDto.getRedirectUris())
                .postLogoutRedirectUris(clientDto.getPostLogoutRedirectUris())
                .scopes(clientDto.getScopes())
                .requireProofKey(clientDto.isRequireProofKey())
                .build();
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        log.warn("save(RegisteredClient) not implemented");
    }

    @Override
    public AppResponseDto createClient(ClientCreateDto clientCreateDto) {
        try {
            Client client = clientFromDto(clientCreateDto);
            clientRepository.save(client);
            log.info("Client [{}] saved successfully with ID: {}", client.getClientName(), client.getId());
            return new AppResponseDto(HttpStatus.CREATED, "Client [" + client.getClientName() + "] saved successfully");
        } catch (Exception e) {
            log.error("Error creating client", e);
            throw new RuntimeException("Error creating client: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisteredClient findById(String id) {
        log.debug("Finding client by id: {}", id);
        try {
            Client client = clientRepository.findById(Long.valueOf(id))
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
            return Client.toRegisteredClient(client);
        } catch (NumberFormatException e) {
            log.error("Invalid client id format: {}", id);
            return null;
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        log.debug("Finding client by clientId: {}", clientId);
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with clientId: " + clientId));
        return Client.toRegisteredClient(client);
    }
}