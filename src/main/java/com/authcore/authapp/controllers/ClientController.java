package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.services.client.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/client")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<AppResponseDto> create(@RequestBody ClientCreateDto dto) {
        log.info("Received request to create client: {}", dto.getClientId());
        try {
            AppResponseDto response = clientService.createClient(dto);
            log.info("Client created successfully: {}", dto.getClientId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating client: {}", dto.getClientId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}