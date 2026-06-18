package com.authcore.authapp.dto.permission.mapper;

import com.authcore.authapp.dto.permission.PermissionResponseDto;
import com.authcore.authapp.models.Permission;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Page<PermissionResponseDto> toResponseDtoPage(Page<Permission> permissions) {
        return permissions.map(this::toResponseDto);
    }

    public PermissionResponseDto toResponseDto(Permission permission) {
        PermissionResponseDto dto = new PermissionResponseDto();
        dto.setId(permission.getId().toString());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setActive(permission.getActive());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }
}
