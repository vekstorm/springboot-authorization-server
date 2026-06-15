package com.authcore.authapp.services.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.dto.client.ClientResponseDto;
import com.authcore.authapp.dto.client.ClientUpdateDto;

public interface ClientService {

    AppResponseDto createClient(ClientCreateDto clientCreateDto);

    Page<ClientResponseDto> getClients(String search, Pageable pageable);

    void deleteClient(String clientId);

    AppResponseDto saveClient(ClientUpdateDto clientResponseDto);

}
