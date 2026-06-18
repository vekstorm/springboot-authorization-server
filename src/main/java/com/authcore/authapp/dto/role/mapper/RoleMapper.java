package com.authcore.authapp.dto.role.mapper;

import com.authcore.authapp.dto.role.RoleResponseDto;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.models.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public Page<RoleResponseDto> toResponseDtoPage(Page<Role> roles) {
        return roles.map(this::toResponseDto);
    }

    public RoleResponseDto toResponseDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId().toString());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.getActive());
        dto.setPermissions(role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
}
