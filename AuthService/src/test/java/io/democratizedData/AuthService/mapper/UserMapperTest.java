package io.democratizedData.AuthService.mapper;

import io.democratizedData.AuthService.model.dto.UserRegisterDto;
import io.democratizedData.AuthService.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testToEntity() {
        // Create a UserRegisterDto with test data
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setFirstName("John");
        registerDto.setLastName("Doe");
        registerDto.setUsername("johndoe");
        registerDto.setEmail("john.doe@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole("USER");
        registerDto.setBirthdate("1990-01-01");
        registerDto.setGender("Male");
        registerDto.setOccupation("Developer");
        registerDto.setCity("New York");

        // Map to User entity
        User user = userMapper.toEntity(registerDto);

        // Verify that all fields are correctly mapped
        System.out.println("[DEBUG_LOG] User: " + user);
        assertNotNull(user, "User should not be null");
        assertEquals(registerDto.getFirstName(), user.getFirstName(), "First name should match");
        assertEquals(registerDto.getLastName(), user.getLastName(), "Last name should match");
        assertEquals(registerDto.getUsername(), user.getUsername(), "Username should match");
        assertEquals(registerDto.getEmail(), user.getEmail(), "Email should match");
        // Password is set separately in the service, so we don't check it here
        assertEquals(registerDto.getRole(), user.getRole(), "Role should match");
        assertEquals(registerDto.getBirthdate(), user.getBirthdate(), "Birthdate should match");
        assertEquals(registerDto.getGender(), user.getGender(), "Gender should match");
        assertEquals(registerDto.getOccupation(), user.getOccupation(), "Occupation should match");
        assertEquals(registerDto.getCity(), user.getCity(), "City should match");
    }
}