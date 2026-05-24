package com.authcore.authapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AppUserCreateDto {

    @NotBlank(message = "username is required")
    @Size(min = 1, max = 50)
    String username;

    @NotBlank
    @Size(min = 1, max = 100)
    String password;

    @NotBlank
    @Email(message = "Must use a valid email")
    String email;

    @Size(min = 1, max = 200)
    String address;

    @Size(min = 1, max = 15)
    String phone;

    boolean isGoogle;

    boolean isMicrosoft;

    boolean isFacebook;

    boolean isGitHub;

    String metadata;

    boolean expired;

    boolean locked;

    boolean credentialsExpired;

    boolean disabled;

    List<String> roles; // "ROLE_ADMIN", "ROLE_USER"

}
