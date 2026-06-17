package com.authcore.authapp.configuration;

import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.models.Client;
import com.authcore.authapp.models.Permission;
import com.authcore.authapp.models.Role;
import com.authcore.authapp.repository.AppUserRepository;
import com.authcore.authapp.repository.ClientRepository;
import com.authcore.authapp.repository.PermissionRepository;
import com.authcore.authapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Value("${defaults.admin-role}")
    private String adminRoleName;

    @Value("${defaults.user-role}")
    private String userRoleName;

    @Value("${defaults.admin-email}")
    private String adminEmail;

    @Value("${defaults.user-email}")
    private String userEmail;

    @Value("${defaults.admin-password}")
    private String adminPassword;

    @Value("${defaults.user-password}")
    private String userPassword;

    @Value("${defaults.client-id}")
    private String defaultClientId;

    @Value("${defaults.client-secret}")
    private String defaultClientSecret;

    @Value("${defaults.client-name}")
    private String defaultClientName;

    @Value("${defaults.client-scope}")
    private String defaultClientScope;

    @Value("${defaults.redirect-uri}")
    private String defaultRedirectUri;

    @Value("${defaults.client-credentials-grant-type}")
    private String clientCredentialsGrantType;

    @Value("${defaults.authorization-code-grant-type}")
    private String authorizationCodeGrantType;

    @Value("${defaults.refresh-token-grant-type}")
    private String refreshTokenGrantType;

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedOidcClient();

        if (appUserRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Default users already exist. Skipping user initialization.");
            return;
        }

        List<Permission> allPermissions = createPermissions();

        Role adminRole = createRole(adminRoleName, "Administrator role with full access", allPermissions);

        List<Permission> readPermissions = allPermissions.stream()
                .filter(p -> p.getName().endsWith(":read") && !p.getName().startsWith("client"))
                .toList();
        Role userRole = createRole(userRoleName, "Standard user role with read-only access", readPermissions);

        createUser("admin", adminEmail, adminPassword, "Admin", adminRole);
        createUser("user", userEmail, userPassword, "User", userRole);

        log.info("Database initialization completed successfully.");
    }

    private void seedOidcClient() {
        Client client = clientRepository.findByClientId(defaultClientId).orElse(null);
        boolean isNew = false;

        if (client == null) {
            client = Client.builder()
                    .clientId(defaultClientId)
                    .clientIdIssuedAt(Instant.now())
                    .build();
            isNew = true;
        }

        client.setClientName(defaultClientName);
        client.setClientSecret(passwordEncoder.encode(defaultClientSecret));
        client.setAuthenticationMethods(Set.of(
                new ClientAuthenticationMethod("client_secret_basic"),
                new ClientAuthenticationMethod("none")));
        client.setAuthorizationGrantTypes(Set.of(
                new AuthorizationGrantType(clientCredentialsGrantType),
                new AuthorizationGrantType(authorizationCodeGrantType),
                new AuthorizationGrantType(refreshTokenGrantType)));
        client.setRedirectUris(Set.of(defaultRedirectUri));
        client.setPostLogoutRedirectUris(Set.of());
        client.setScopes(Set.of(defaultClientScope));
        client.setRequireProofKey(true);

        clientRepository.save(client);
        log.info("{} OIDC client: {}", isNew ? "Created" : "Updated", defaultClientId);
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
