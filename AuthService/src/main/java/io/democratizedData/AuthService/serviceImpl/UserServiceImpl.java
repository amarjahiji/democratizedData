package io.democratizedData.AuthService.serviceImpl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.democratizedData.AuthService.mapper.UserMapper;
import io.democratizedData.AuthService.model.dto.UserLoginDto;
import io.democratizedData.AuthService.model.dto.UserRegisterDto;
import io.democratizedData.AuthService.model.entity.User;
import io.democratizedData.AuthService.repository.UserRepository;
import io.democratizedData.AuthService.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Value("${jwt.secret}")
    private String jwtSecret;


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
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
}
