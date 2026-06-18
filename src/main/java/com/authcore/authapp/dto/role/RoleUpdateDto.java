package com.authcore.authapp.dto.role;

import lombok.Data;
import java.util.Set;

@Data
public class RoleUpdateDto {

    private String id;

    private String name;

    private String description;

    private Set<String> permissions;

    private Boolean active;
}
