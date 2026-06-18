package com.authcore.authapp.dto.role;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class RoleResponseDto {

    private String id;

    private String name;

    private String description;

    private Set<String> permissions;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
