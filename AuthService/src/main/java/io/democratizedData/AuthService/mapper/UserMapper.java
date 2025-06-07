package io.democratizedData.AuthService.mapper;

import io.democratizedData.AuthService.model.dto.UserRegisterDto;
import io.democratizedData.AuthService.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegisterDto userRegisterDto);
}
