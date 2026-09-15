package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.Role;

public record UserFilter(
    String username,
    Role role,
    Boolean isActive
)
{
}
