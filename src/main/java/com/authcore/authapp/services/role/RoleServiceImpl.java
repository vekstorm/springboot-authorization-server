package com.authcore.authapp.services.role;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.role.RoleCreateDto;
import com.authcore.authapp.dto.role.RoleResponseDto;
import com.authcore.authapp.dto.role.RoleUpdateDto;
import com.authcore.authapp.dto.role.mapper.RoleMapper;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.models.Role;
import com.authcore.authapp.repository.AppUserRepository;
import com.authcore.authapp.repository.PermissionRepository;
import com.authcore.authapp.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AppUserRepository appUserRepository;
    private final RoleMapper roleMapper;

    @Override
    public AppResponseDto createRole(RoleCreateDto dto) {
        try {
            if (roleRepository.existsByName(dto.getName())) {
                return new AppResponseDto(HttpStatus.CONFLICT, "Role [" + dto.getName() + "] already exists");
            }

            Set<Permission> permissions = resolvePermissions(dto.getPermissions());

            Role role = Role.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .permissions(permissions)
                    .active(dto.getActive() != null ? dto.getActive() : true)
                    .build();

            roleRepository.save(role);
            log.info("Role [{}] created successfully with ID: {}", role.getName(), role.getId());
            return new AppResponseDto(HttpStatus.CREATED, "Role [" + role.getName() + "] created successfully");
        } catch (Exception e) {
            log.error("Error creating role", e);
            throw new RuntimeException("Error creating role: " + e.getMessage(), e);
        }
    }

    @Override
    public RoleResponseDto getRole(String id) {
        Role role = roleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return roleMapper.toResponseDto(role);
    }

    @Override
    public Page<RoleResponseDto> getRoles(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return roleMapper.toResponseDtoPage(roleRepository.findAll(pageable));
        }
        return roleMapper.toResponseDtoPage(roleRepository.findByNameContainingIgnoreCase(search, pageable));
    }

    @Override
    public AppResponseDto updateRole(RoleUpdateDto dto) {
        try {
            Role role = roleRepository.findById(UUID.fromString(dto.getId()))
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + dto.getId()));

            if (dto.getName() != null && !dto.getName().equals(role.getName())) {
                if (roleRepository.existsByName(dto.getName())) {
                    return new AppResponseDto(HttpStatus.CONFLICT, "Role name [" + dto.getName() + "] already exists");
                }
                role.setName(dto.getName());
            }
            if (dto.getDescription() != null) {
                role.setDescription(dto.getDescription());
            }
            if (dto.getPermissions() != null) {
                role.setPermissions(resolvePermissions(dto.getPermissions()));
            }
            if (dto.getActive() != null) {
                role.setActive(dto.getActive());
            }

            roleRepository.save(role);
            log.info("Role [{}] updated successfully", role.getName());
            return new AppResponseDto(HttpStatus.OK, "Role [" + role.getName() + "] updated successfully");
        } catch (Exception e) {
            log.error("Error updating role", e);
            throw new RuntimeException("Error updating role: " + e.getMessage(), e);
        }
    }

    @Override
    public AppResponseDto deleteRole(String id) {
        try {
            Role role = roleRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

            if (appUserRepository.existsByRolesId(role.getId())) {
                return new AppResponseDto(HttpStatus.CONFLICT,
                        "Cannot delete role [" + role.getName() + "] because it is assigned to one or more users");
            }

            roleRepository.delete(role);
            log.info("Role [{}] deleted successfully", role.getName());
            return new AppResponseDto(HttpStatus.OK, "Role [" + role.getName() + "] deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting role", e);
            throw new RuntimeException("Error deleting role: " + e.getMessage(), e);
        }
    }

    private Set<Permission> resolvePermissions(Set<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return Set.of();
        }
        return permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + name)))
                .collect(Collectors.toSet());
    }
}
