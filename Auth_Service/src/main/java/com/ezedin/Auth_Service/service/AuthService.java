package com.ezedin.Auth_Service.service;

import com.ezedin.Auth_Service.config.JwtProperties;
import com.ezedin.Auth_Service.dto.*;
import com.ezedin.Auth_Service.exception.InvalidCredentialsException;
import com.ezedin.Auth_Service.exception.UserNotFoundException;
import com.ezedin.Auth_Service.grpc.UserGrpcClient;
import com.ezedin.Auth_Service.security.JwtService;
import com.ezedin.grpc.user.UserGrpcResponse;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserGrpcClient userGrpcClient;

    public LoginResponse login(@Valid LoginRequest request) {
        UserInfo user = getUserByUsername(request.getUsername());

        if (!passwordsMatch(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtService.generateToken(user);
        Instant expiresAt = Instant.now().plusMillis(jwtProperties.getExpiration());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .build();
    }
    public CreateUserResponse register(@Valid CreateUserRequest request) {
        CreateUserRequest requestWithHashedPassword = new CreateUserRequest(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        CreateUserResponse user = userGrpcClient.createUser(requestWithHashedPassword);

        return CreateUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private UserInfo getUserByUsername(String username) {
        try {
            UserGrpcResponse user = userGrpcClient.findByUsername(username);
            return UserInfo.builder()
                    .id(UUID.fromString(user.getId()))
                    .username(user.getUsername())
                    .role(user.getRole())
                    .password(user.getPassword())
                    .build();
        } catch (StatusRuntimeException | UserNotFoundException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private boolean passwordsMatch(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}