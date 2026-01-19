package com.ecommerce.api.dto;

import com.ecommerce.api.entity.enums.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private Role role;
}
