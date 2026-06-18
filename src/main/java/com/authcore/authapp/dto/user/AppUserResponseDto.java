package com.authcore.authapp.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class AppUserResponseDto {

    private String id;

    private String username;

    private String email;

    private String name;

    private String surname1;

    private String surname2;

    private String address;

    private String phone;

    private boolean isGoogle;

    private boolean isMicrosoft;

    private boolean isFacebook;

    private boolean isGitHub;

    private String metadata;

    private boolean expired;

    private boolean locked;

    private boolean credentialsExpired;

    private boolean disabled;

    private Set<String> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
