package com.authcore.authapp.services.role;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.role.RoleCreateDto;
import com.authcore.authapp.dto.role.RoleResponseDto;
import com.authcore.authapp.dto.role.RoleUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    AppResponseDto createRole(RoleCreateDto dto);

    RoleResponseDto getRole(String id);

    Page<RoleResponseDto> getRoles(String search, Pageable pageable);

    AppResponseDto updateRole(RoleUpdateDto dto);

    AppResponseDto deleteRole(String id);
}
