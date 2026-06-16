package com.authcore.authapp.configuration;

import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.models.Role;
import com.authcore.authapp.repository.AppUserRepository;
import com.authcore.authapp.repository.PermissionRepository;
import com.authcore.authapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (appUserRepository.findByEmail("admin@email.com").isPresent()) {
            log.info("Default users already exist. Skipping data initialization.");
            return;
        }

        List<Permission> allPermissions = createPermissions();

        Role adminRole = createRole("ROLE_ADMIN", "Administrator role with full access", allPermissions);

        List<Permission> readPermissions = allPermissions.stream()
                .filter(p -> p.getName().endsWith(":read") && !p.getName().startsWith("client"))
                .toList();
        Role userRole = createRole("ROLE_USER", "Standard user role with read-only access", readPermissions);

        createUser("admin", "admin@email.com", "admin123", "Admin", adminRole);
        createUser("user", "user@email.com", "user123", "User", userRole);

        log.info("Database initialization completed successfully.");
    }

    private List<Permission> createPermissions() {
        List<Permission> permissions = new ArrayList<>();
        permissions.add(createPermissionIfNotExists("user:read", "View user details"));
        permissions.add(createPermissionIfNotExists("user:write", "Create and update users"));
        permissions.add(createPermissionIfNotExists("user:delete", "Delete users"));
        permissions.add(createPermissionIfNotExists("client:read", "View OAuth2 clients"));
        permissions.add(createPermissionIfNotExists("client:write", "Create and update OAuth2 clients"));
        permissions.add(createPermissionIfNotExists("client:delete", "Delete OAuth2 clients"));
        permissions.add(createPermissionIfNotExists("role:read", "View roles"));
        permissions.add(createPermissionIfNotExists("role:write", "Create and update roles"));
        permissions.add(createPermissionIfNotExists("role:delete", "Delete roles"));
        permissions.add(createPermissionIfNotExists("permission:read", "View permissions"));
        permissions.add(createPermissionIfNotExists("permission:write", "Create and update permissions"));
        permissions.add(createPermissionIfNotExists("permission:delete", "Delete permissions"));
        return permissions;
    }

    private Permission createPermissionIfNotExists(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .name(name)
                                .description(description)
                                .active(true)
                                .createdAt(null) // Will be set automatically by @CreatedDate
                                .build()));
    }

    private Role createRole(String name, String description, List<Permission> permissions) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(name)
                                .description(description)
                                .permissions(Set.copyOf(permissions))
                                .createdAt(null) // Will be set automatically by @CreatedDate
                                .active(true)
                                .build()));
    }

    private void createUser(String username, String email, String password, String name, Role role) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            log.info("User {} already exists.", email);
            return;
        }
        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .roles(Set.of(role))
                .build();
        appUserRepository.save(user);
        log.info("Created user: {}", email);
    }
}
