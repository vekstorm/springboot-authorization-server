package com.authcore.authapp.dto.role;

import lombok.Data;
import java.util.Set;

@Data
public class RoleCreateDto {

    private String name;

    private String description;

    private Set<String> permissions;

    private Boolean active;
}
