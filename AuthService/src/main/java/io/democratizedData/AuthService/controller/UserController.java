package io.democratizedData.AuthService.controller;

import io.democratizedData.AuthService.model.dto.UserLoginDto;
import io.democratizedData.AuthService.model.dto.UserRegisterDto;
import io.democratizedData.AuthService.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody UserRegisterDto registerDto) {
        String response = userService.register(registerDto);
        return response != null ? ResponseEntity.ok(Collections.singletonMap("token", response)) : ResponseEntity.status(401).body("User already exists with those credentials");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        String response = userService.login(loginDto);
        return response != null ? ResponseEntity.ok(Collections.singletonMap("token", response)) : ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> userDetails = userService.validateTokenGetUser(authHeader);
        return ResponseEntity.ok(userDetails);
    }
}