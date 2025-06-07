package io.democratizedData.AuthService.service;

import io.democratizedData.AuthService.model.dto.UserLoginDto;
import io.democratizedData.AuthService.model.dto.UserRegisterDto;

public interface UserService {

    String register(UserRegisterDto registerDto);

    String login(UserLoginDto loginDto);
}
