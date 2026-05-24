package com.authcore.authapp.services.client;


import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;

public interface ClientService {

    AppResponseDto createClient(ClientCreateDto clientCreateDto);
}
