package io.democratizedData.AuthService.model.dto;

import lombok.Data;

@Data
public class UserRegisterDto {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String role;
    private String birthdate;
    private String gender;
    private String occupation;
    private String city;
}
