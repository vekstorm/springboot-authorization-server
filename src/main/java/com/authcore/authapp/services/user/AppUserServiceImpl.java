package com.authcore.authapp.services.user;

import com.authcore.authapp.dto.AppResponseDto;
import com.authcore.authapp.dto.user.AppUserCreateDto;
import com.authcore.authapp.models.AppUser;
import com.authcore.authapp.models.Role;
import com.authcore.authapp.repository.AppUserRepository;
import com.authcore.authapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppResponseDto createUser(AppUserCreateDto dto) {
        AppUser appUser = AppUser.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .isGoogle(dto.isGoogle())
                .isMicrosoft(dto.isMicrosoft())
                .isFacebook(dto.isFacebook())
                .isGitHub(dto.isGitHub())
                .expired(dto.isExpired())
                .locked(dto.isLocked())
                .credentialsExpired(dto.isCredentialsExpired())
                .disabled(dto.isDisabled())
                .build();

        Set<Role> roles = new HashSet<>();
        dto.getRoles().forEach(rol -> {
            Role role = roleRepository.findByName(rol).orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(role);
        });
        appUser.setRoles(roles);
        appUserRepository.save(appUser);
        return new AppResponseDto(HttpStatus.OK, "User created successfully");
    }

    @Override
    @Transactional
    public AppUser findOrCreateOAuth2User(String email, String givenName, String familyName) {
        log.info("findOrCreateOAuth2User called for email: {}", email);

        var existing = appUserRepository.findByEmail(email);
        if (existing.isPresent()) {
            log.info("Existing user found for email: {}, isGoogle: {}", email, existing.get().isGoogle());
            return existing.get();
        }

        log.info("No existing user found for email: {}, creating new one", email);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
        log.info("Found role: {}", userRole.getName());

        String username = email.split("@")[0];
        String baseUsername = username;
        int suffix = 1;
        while (appUserRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }
        log.info("Generated username: {}", username);

        AppUser newUser = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .name(givenName)
                .surname1(familyName)
                .isGoogle(true)
                .roles(Set.of(userRole))
                .build();

        AppUser saved = appUserRepository.saveAndFlush(newUser);
        log.info("New Google user created and flushed, id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
