package com.mlcdev.realestate.api.dto;

import com.mlcdev.realestate.api.entities.Role;

public record UserFilter(
    String username,
    Role role,
    Boolean isActive
)
{
}
