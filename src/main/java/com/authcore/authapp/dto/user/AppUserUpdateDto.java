package com.authcore.authapp.dto.user;

import lombok.Data;

import java.util.Set;

@Data
public class AppUserUpdateDto {

    private String username;

    private String email;

    private String password;

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
}
