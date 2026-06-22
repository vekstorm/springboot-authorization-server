package com.authcore.authapp.services.client;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.dto.client.ClientResponseDto;
import com.authcore.authapp.dto.client.ClientUpdateDto;
import com.authcore.authapp.dto.client.mapper.ClientMapper;
import com.authcore.authapp.models.Client;
import com.authcore.authapp.repository.ClientRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class ClientServiceImpl implements ClientService, RegisteredClientRepository {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientMapper clientMapper;

    private Client clientFromDto(ClientCreateDto clientDto) {
        return Client.builder()
                .clientId(clientDto.getClientId())
                .clientSecret(clientDto.getClientSecret() != null ? passwordEncoder.encode(clientDto.getClientSecret()) : null)
                .clientName(clientDto.getClientName())
                .clientIdIssuedAt(Instant.now())
                .authorizationGrantTypes(
                        ClientMapper.getAuthorizationGrantTypesFromNamesList(clientDto.getAuthorizationGrantTypes()))
                .authenticationMethods(
                        ClientMapper.getClientAuthenticationMethodsFromNamesList(clientDto.getAuthenticationMethods()))
                .redirectUris(clientDto.getRedirectUris())
                .postLogoutRedirectUris(clientDto.getPostLogoutRedirectUris())
                .scopes(clientDto.getScopes())
                .requireProofKey(clientDto.isRequireProofKey())
                .build();
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = clientRepository.findById(UUID.fromString(registeredClient.getId()))
                .orElse(null);
        if (client == null) {
            client = clientRepository.findByClientId(registeredClient.getClientId())
                    .orElse(null);
        }
        if (client == null) {
            client = Client.builder()
                    .clientId(registeredClient.getClientId())
                    .clientIdIssuedAt(registeredClient.getClientIdIssuedAt() != null
                            ? registeredClient.getClientIdIssuedAt() : Instant.now())
                    .build();
        }
        client.setClientSecret(registeredClient.getClientSecret());
        client.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        client.setClientName(registeredClient.getClientName());
        if (registeredClient.getClientAuthenticationMethods() != null) {
            client.setAuthenticationMethods(registeredClient.getClientAuthenticationMethods());
        }
        if (registeredClient.getAuthorizationGrantTypes() != null) {
            client.setAuthorizationGrantTypes(registeredClient.getAuthorizationGrantTypes());
        }
        if (registeredClient.getRedirectUris() != null) {
            client.setRedirectUris(registeredClient.getRedirectUris());
        }
        if (registeredClient.getPostLogoutRedirectUris() != null) {
            client.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris());
        }
        if (registeredClient.getScopes() != null) {
            client.setScopes(registeredClient.getScopes());
        }
        client.setRequireProofKey(registeredClient.getClientSettings() != null
                && registeredClient.getClientSettings().isRequireProofKey());
        clientRepository.save(client);
    }

    @Override
    public AppResponseDto createClient(ClientCreateDto clientCreateDto) {
        try {
            Client client = clientFromDto(clientCreateDto);
            client = clientRepository.save(client);
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
            Client client = clientRepository.findById(UUID.fromString(id))
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
        RegisteredClient rc = Client.toRegisteredClient(client);
        log.warn(">>> grants para {}: {}", clientId, rc.getAuthorizationGrantTypes());
        log.warn(">>> tokenSettings: {}", rc.getTokenSettings().getSettings());
        return rc;
    }

    @Override
    public Page<ClientResponseDto> getClients(String clientName, Pageable pageable) {
        if (clientName == null || clientName.isEmpty()) {
            log.debug("Finding clients by clientName: {} - Page {}: Size {}", clientName, pageable.getPageNumber(),
                    pageable.getPageSize());
            return clientMapper.toClientResponseDtoPage(clientRepository.findAll(pageable));
        }
        return clientMapper
                .toClientResponseDtoPage(clientRepository.findByClientNameContainingIgnoreCase(clientName, pageable));
    }

    @Override
    public void deleteClient(String clientId) {
        clientRepository.deleteByClientId(clientId);
    }

    @Override
    public AppResponseDto saveClient(ClientUpdateDto clientUpdateDto) {
        try {
            Client client = clientRepository.findByClientId(clientUpdateDto.getClientId())
                    .orElseThrow(() -> new RuntimeException(
                            "Client not found with clientId: " + clientUpdateDto.getClientId()));

            client.setClientName(clientUpdateDto.getClientName());
            if (clientUpdateDto.getClientSecret() != null && !clientUpdateDto.getClientSecret().isEmpty()) {
                client.setClientSecret(passwordEncoder.encode(clientUpdateDto.getClientSecret()));
            }
            client.setAuthorizationGrantTypes(
                    ClientMapper.getAuthorizationGrantTypesFromNamesList(clientUpdateDto.getAuthorizationGrantTypes()));
            client.setAuthenticationMethods(ClientMapper
                    .getClientAuthenticationMethodsFromNamesList(clientUpdateDto.getAuthenticationMethods()));
            client.setRedirectUris(clientUpdateDto.getRedirectUris());
            client.setPostLogoutRedirectUris(clientUpdateDto.getPostLogoutRedirectUris());
            client.setScopes(clientUpdateDto.getScopes());
            client.setRequireProofKey(clientUpdateDto.isRequireProofKey());

            clientRepository.save(client);
            log.info("Client [{}] updated successfully with ID: {}", client.getClientName(), client.getId());
            return new AppResponseDto(HttpStatus.OK, "Client [" + client.getClientName() + "] updated successfully");
        } catch (Exception e) {
            log.error("Error updating client", e);
            throw new RuntimeException("Error updating client: " + e.getMessage(), e);
        }
    }

}