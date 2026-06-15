package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.client.ClientCreateDto;
import com.authcore.authapp.dto.client.ClientResponseDto;
import com.authcore.authapp.dto.client.ClientUpdateDto;
import com.authcore.authapp.services.client.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
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

    @PutMapping
    public ResponseEntity<AppResponseDto> update(@RequestBody ClientUpdateDto dto) {
        log.info("Received request to update client: {}", dto.getClientId());
        try {
            AppResponseDto response = clientService.saveClient(dto);
            log.info("Client updated successfully: {}", dto.getClientId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error updating client: {}", dto.getClientId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ClientResponseDto>> getAll(
            @RequestParam(required = false) String clientName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get clients with clientName: {}, page: {}, size: {}", clientName, page, size);
        try {
            Page<ClientResponseDto> clients = clientService.getClients(clientName,
                    org.springframework.data.domain.PageRequest.of(page, size));
            log.info("Clients retrieved successfully with clientName: {}, page: {}, size: {}", clientName, page, size);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            log.error("Error retrieving clients with clientName: {}, page: {}, size: {}", clientName, page, size, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<AppResponseDto> deleteClient(@PathVariable String clientId) {
        log.info("Received request to delete client with ID: {}", clientId);
        try {
            clientService.deleteClient(clientId);
            log.info("Client deleted successfully with ID: {}", clientId);
            return ResponseEntity.ok(new AppResponseDto(HttpStatus.OK, "Client deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting client with ID: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}