package com.authcore.authapp.services.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.dto.client.ClientResponseDto;

public interface ClientService {

    AppResponseDto createClient(ClientCreateDto clientCreateDto);

    Page<ClientResponseDto> getClients(String search, Pageable pageable);

}
