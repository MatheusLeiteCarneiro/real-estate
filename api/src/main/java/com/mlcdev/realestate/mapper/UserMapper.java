package com.mlcdev.realestate.mapper;

import com.mlcdev.realestate.dto.UserDTO;
import com.mlcdev.realestate.entities.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDTO entityToDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .authorities(entity.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .createdAt(entity.getCreatedAt())
                .active(entity.isActive())
                .build();
    }
}
