package com.authcore.authapp.dto.permission;

import lombok.Data;

@Data
public class PermissionCreateDto {

    private String name;

    private String description;

    private Boolean active;
}
