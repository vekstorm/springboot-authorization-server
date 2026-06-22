package com.authcore.authapp.services.permission;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.permission.PermissionCreateDto;
import com.authcore.authapp.dto.permission.PermissionResponseDto;
import com.authcore.authapp.dto.permission.PermissionUpdateDto;
import com.authcore.authapp.dto.permission.mapper.PermissionMapper;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public AppResponseDto createPermission(PermissionCreateDto dto) {
        try {
            if (permissionRepository.existsByName(dto.getName())) {
                return new AppResponseDto(HttpStatus.CONFLICT, "Permission [" + dto.getName() + "] already exists");
            }

            Permission permission = Permission.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();

            permissionRepository.save(permission);
            log.info("Permission [{}] created successfully with ID: {}", permission.getName(), permission.getId());
            return new AppResponseDto(HttpStatus.CREATED, "Permission [" + permission.getName() + "] created successfully");
        } catch (Exception e) {
            log.error("Error creating permission", e);
            throw new RuntimeException("Error creating permission: " + e.getMessage(), e);
        }
    }

    @Override
    public PermissionResponseDto getPermission(String id) {
        Permission permission = permissionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
        return permissionMapper.toResponseDto(permission);
    }

    @Override
    public Page<PermissionResponseDto> getPermissions(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return permissionMapper.toResponseDtoPage(permissionRepository.findAll(pageable));
        }
        return permissionMapper.toResponseDtoPage(permissionRepository.findByNameContainingIgnoreCase(search, pageable));
    }

    @Override
    public AppResponseDto updatePermission(PermissionUpdateDto dto) {
        try {
            Permission permission = permissionRepository.findById(UUID.fromString(dto.getId()))
                    .orElseThrow(() -> new RuntimeException("Permission not found with id: " + dto.getId()));

            if (dto.getName() != null && !dto.getName().equals(permission.getName())) {
                if (permissionRepository.existsByName(dto.getName())) {
                    return new AppResponseDto(HttpStatus.CONFLICT, "Permission name [" + dto.getName() + "] already exists");
                }
                permission.setName(dto.getName());
            }
            if (dto.getDescription() != null) {
                permission.setDescription(dto.getDescription());
            }
            if (dto.getActive() != null) {
                permission.setActive(dto.getActive());
            }

            permissionRepository.save(permission);
            log.info("Permission [{}] updated successfully", permission.getName());
            return new AppResponseDto(HttpStatus.OK, "Permission [" + permission.getName() + "] updated successfully");
        } catch (Exception e) {
            log.error("Error updating permission", e);
            throw new RuntimeException("Error updating permission: " + e.getMessage(), e);
        }
    }

    @Override
    public AppResponseDto deletePermission(String id) {
        try {
            Permission permission = permissionRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
            permissionRepository.delete(permission);
            log.info("Permission [{}] deleted successfully", permission.getName());
            return new AppResponseDto(HttpStatus.OK, "Permission [" + permission.getName() + "] deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting permission", e);
            throw new RuntimeException("Error deleting permission: " + e.getMessage(), e);
        }
    }

    @Override
    public AppResponseDto deletePermissions(List<String> ids) {
        try {
            List<Permission> permissions = permissionRepository.findAllById(
                    ids.stream().map(UUID::fromString).toList());
            permissionRepository.deleteAll(permissions);
            log.info("Batch deleted {} permissions", permissions.size());
            return new AppResponseDto(HttpStatus.OK,
                    permissions.size() + " permissions deleted successfully");
        } catch (Exception e) {
            log.error("Error batch deleting permissions", e);
            throw new RuntimeException("Error batch deleting permissions: " + e.getMessage(), e);
        }
    }
}
