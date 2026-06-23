package com.authcore.authapp.controllers;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.role.RoleCreateDto;
import com.authcore.authapp.dto.role.RoleResponseDto;
import com.authcore.authapp.dto.role.RoleUpdateDto;
import com.authcore.authapp.services.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/role", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<AppResponseDto> create(@RequestBody RoleCreateDto dto) {
        log.info("Received request to create role: {}", dto.getName());
        try {
            AppResponseDto response = roleService.createRole(dto);
            log.info("Role created successfully: {}", dto.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating role: {}", dto.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<RoleResponseDto> getById(@PathVariable String id) {
        log.info("Received request to get role by id: {}", id);
        try {
            RoleResponseDto role = roleService.getRole(id);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            log.error("Error getting role by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Page<RoleResponseDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Received request to get roles with search: {}, page: {}, size: {}", search, page, size);
        try {
            Page<RoleResponseDto> roles = roleService.getRoles(search,
                    org.springframework.data.domain.PageRequest.of(page, size));
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Error getting roles with search: {}, page: {}, size: {}", search, page, size, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<AppResponseDto> update(@PathVariable String id, @RequestBody RoleUpdateDto dto) {
        dto.setId(id);
        log.info("Received request to update role with id: {}", id);
        try {
            AppResponseDto response = roleService.updateRole(dto);
            log.info("Role updated successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating role with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<AppResponseDto> delete(@PathVariable String id) {
        log.info("Received request to delete role with id: {}", id);
        try {
            AppResponseDto response = roleService.deleteRole(id);
            HttpStatus status = response.getStatus();
            if (status == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            log.info("Role deleted successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting role with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }
}
