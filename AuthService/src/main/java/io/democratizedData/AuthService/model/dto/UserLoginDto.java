package io.democratizedData.AuthService.model.dto;

import lombok.Data;

@Data
public class UserLoginDto {

    private String username;
    private String email;
    private String password;

}
