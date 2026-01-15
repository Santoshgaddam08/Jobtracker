package com.santosh.jobtracker.auth;

import com.santosh.jobtracker.config.JwtService;
import com.santosh.jobtracker.users.User;
import com.santosh.jobtracker.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        userRepository.findByEmail(req.email().toLowerCase()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });

        User user = new User(
                req.email().toLowerCase(),
                passwordEncoder.encode(req.password()),
                req.fullName()
        );

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthDtos.AuthResponse(token, user.getId(), user.getEmail(), user.getFullName());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthDtos.AuthResponse(token, user.getId(), user.getEmail(), user.getFullName());
    }
}
