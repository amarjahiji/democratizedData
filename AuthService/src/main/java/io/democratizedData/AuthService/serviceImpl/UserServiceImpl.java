package io.democratizedData.AuthService.serviceImpl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.democratizedData.AuthService.mapper.UserMapper;
import io.democratizedData.AuthService.model.dto.UserLoginDto;
import io.democratizedData.AuthService.model.dto.UserRegisterDto;
import io.democratizedData.AuthService.model.entity.User;
import io.democratizedData.AuthService.repository.UserRepository;
import io.democratizedData.AuthService.service.UserService;
import io.democratizedData.AuthService.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Value("${jwt.secret}")
    private String jwtSecret;


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(UserRegisterDto registerDto) {
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()
                || userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            return null;
        }
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        userRepository.save(user);
        return generateToken(user);
    }

    @Override
    public String login(UserLoginDto loginDto) {
        Optional<User> foundUser = Optional.empty();
        if (loginDto.getEmail() != null) {
            foundUser = userRepository.findByEmail(loginDto.getEmail());
        } else if (loginDto.getUsername() != null) {
            foundUser = userRepository.findByUsername(loginDto.getUsername());
        }
        return foundUser
                .filter(user -> passwordEncoder.matches(loginDto.getPassword(), user.getPassword()))
                .map(this::generateToken)
                .orElse(null);
    }

    private String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public Map<String, Object> validateTokenGetUser(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Algorithm algorithm = Algorithm.HMAC256("secret-key");
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        String userId = jwt.getClaim("userId").asString();
        String role = jwt.getClaim("role").asString();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", userId);
        userDetails.put("role", role);
        userDetails.put("city", user.getCity());
        userDetails.put("gender", user.getGender());
        userDetails.put("birthdate", user.getBirthdate());
        return userDetails;
    }

}
