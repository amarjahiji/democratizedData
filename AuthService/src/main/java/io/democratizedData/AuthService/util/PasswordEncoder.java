package io.democratizedData.AuthService.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordEncoder {
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Encodes the raw password using SHA-256 with a random salt.
     * 
     * @param rawPassword the password to encode
     * @return the encoded password with salt (format: salt:hash)
     */
    public String encode(String rawPassword) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltString = Base64.getEncoder().encodeToString(salt);
        
        String hash = hashPassword(rawPassword, salt);
        return saltString + ":" + hash;
    }
    
    /**
     * Verifies if the raw password matches the encoded password.
     * 
     * @param rawPassword the password to check
     * @param encodedPassword the encoded password to check against (format: salt:hash)
     * @return true if the passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String expectedHash = parts[1];
        String actualHash = hashPassword(rawPassword, salt);
        
        return expectedHash.equals(actualHash);
    }
    
    private String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}