package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.permission.PermissionCreateDto;
import com.authcore.authapp.dto.permission.PermissionResponseDto;
import com.authcore.authapp.dto.permission.PermissionUpdateDto;
import com.authcore.authapp.services.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('permission:write')")
    public ResponseEntity<AppResponseDto> create(@RequestBody PermissionCreateDto dto) {
        log.info("Received request to create permission: {}", dto.getName());
        try {
            AppResponseDto response = permissionService.createPermission(dto);
            log.info("Permission created successfully: {}", dto.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating permission: {}", dto.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<PermissionResponseDto> getById(@PathVariable String id) {
        log.info("Received request to get permission by id: {}", id);
        try {
            PermissionResponseDto permission = permissionService.getPermission(id);
            return ResponseEntity.ok(permission);
        } catch (Exception e) {
            log.error("Error getting permission by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Page<PermissionResponseDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get permissions with search: {}, page: {}, size: {}", search, page, size);
        try {
            Page<PermissionResponseDto> permissions = permissionService.getPermissions(search,
                    org.springframework.data.domain.PageRequest.of(page, size));
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Error getting permissions with search: {}, page: {}, size: {}", search, page, size, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:write')")
    public ResponseEntity<AppResponseDto> update(@PathVariable String id, @RequestBody PermissionUpdateDto dto) {
        dto.setId(id);
        log.info("Received request to update permission with id: {}", id);
        try {
            AppResponseDto response = permissionService.updatePermission(dto);
            log.info("Permission updated successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating permission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<AppResponseDto> delete(@PathVariable String id) {
        log.info("Received request to delete permission with id: {}", id);
        try {
            AppResponseDto response = permissionService.deletePermission(id);
            HttpStatus status = response.getStatus();
            if (status == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            log.info("Permission deleted successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting permission with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<AppResponseDto> deleteBatch(@RequestBody List<String> ids) {
        log.info("Received request to batch delete {} permissions", ids.size());
        try {
            AppResponseDto response = permissionService.deletePermissions(ids);
            HttpStatus status = response.getStatus();
            if (status == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            log.info("Batch delete result: {}", response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error batch deleting permissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
