package com.authcore.authapp.services.permission;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.permission.PermissionCreateDto;
import com.authcore.authapp.dto.permission.PermissionResponseDto;
import com.authcore.authapp.dto.permission.PermissionUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionService {

    AppResponseDto createPermission(PermissionCreateDto dto);

    PermissionResponseDto getPermission(String id);

    Page<PermissionResponseDto> getPermissions(String search, Pageable pageable);

    AppResponseDto updatePermission(PermissionUpdateDto dto);

    AppResponseDto deletePermission(String id);

    AppResponseDto deletePermissions(List<String> ids);
}
