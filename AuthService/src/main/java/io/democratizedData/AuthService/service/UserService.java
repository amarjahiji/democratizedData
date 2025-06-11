package io.democratizedData.AuthService.service;

import io.democratizedData.AuthService.model.dto.UserLoginDto;
import io.democratizedData.AuthService.model.dto.UserRegisterDto;

import java.util.Map;

public interface UserService {

    String register(UserRegisterDto registerDto);

    String login(UserLoginDto loginDto);

    Map<String, Object> validateTokenGetUser(String authHeader);
}
