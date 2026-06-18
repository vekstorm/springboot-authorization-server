package com.authcore.authapp.dto.permission;

import lombok.Data;

@Data
public class PermissionUpdateDto {

    private String id;

    private String name;

    private String description;

    private Boolean active;
}
