package com.ecommerce.api.mappers;

import com.ecommerce.api.dto.UserResponseDTO;
import com.ecommerce.api.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO userToUserResponseDTO(User user);
}
