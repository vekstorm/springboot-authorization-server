package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.dto.user.AppUserResponseDto;
import com.authcore.authapp.dto.user.AppUserUpdateDto;
import com.authcore.authapp.services.user.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AppUserService appUserService;

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AppResponseDto> create(@RequestBody AppUserCreateDto dto) {
        log.info("Received request to create user: {}", dto.getUsername());
        try {
            AppResponseDto response = appUserService.createUser(dto);
            log.info("User created successfully: {}", dto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating user: {}", dto.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<AppUserResponseDto> getById(@PathVariable String id) {
        log.info("Received request to get user by id: {}", id);
        try {
            AppUserResponseDto user = appUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error getting user by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Page<AppUserResponseDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get users with search: {}, page: {}, size: {}", search, page, size);
        try {
            Page<AppUserResponseDto> users = appUserService.getUsers(search,
                    org.springframework.data.domain.PageRequest.of(page, size));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users with search: {}, page: {}, size: {}", search, page, size, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AppResponseDto> update(@PathVariable String id, @RequestBody AppUserUpdateDto dto) {
        log.info("Received request to update user with id: {}", id);
        try {
            AppResponseDto response = appUserService.updateUser(id, dto);
            log.info("User updated successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<AppResponseDto> delete(@PathVariable String id) {
        log.info("Received request to delete user with id: {}", id);
        try {
            AppResponseDto response = appUserService.deleteUser(id);
            log.info("User deleted successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
