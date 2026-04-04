package com.loanmanagement.user.dto;

import com.loanmanagement.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO userToUserResponseDTO(User user);
}
