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

import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
@Service
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppResponseDto createUser(AppUserCreateDto dto){
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
        dto.getRoles().forEach( rol -> {
            Role role = roleRepository.findByName(rol).orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(role);
        });
        appUser.setRoles(roles);
        appUserRepository.save(appUser);
        return new AppResponseDto(HttpStatus.OK, "User created successfully");
    }
}
