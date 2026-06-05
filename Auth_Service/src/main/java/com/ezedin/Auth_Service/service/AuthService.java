package com.ezedin.Auth_Service.service;

import com.ezedin.Auth_Service.config.JwtProperties;
import com.ezedin.Auth_Service.dto.*;
import com.ezedin.Auth_Service.exception.InvalidCredentialsException;
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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

        CreateUserResponse user = userGrpcClient.createUser(request);


        return CreateUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private UserInfo getUserByUsername(String username) {
        try{
            UserGrpcResponse user = userGrpcClient.findByUsername(username);
            UUID userID = UUID.fromString(user.getId());
            return UserInfo.builder()
                    .id(userID)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .password(user.getPassword())
                    .build();
        }catch (StatusRuntimeException e) {
            throw new RuntimeException("User not found");
        }

    }

}