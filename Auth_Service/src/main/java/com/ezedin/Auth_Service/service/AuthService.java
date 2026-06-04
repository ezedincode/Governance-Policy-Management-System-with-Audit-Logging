package com.ezedin.Auth_Service.service;

import com.ezedin.Auth_Service.config.JwtProperties;
import com.ezedin.Auth_Service.dto.LoginRequest;
import com.ezedin.Auth_Service.dto.LoginResponse;
import com.ezedin.Auth_Service.dto.UserInfo;
import com.ezedin.Auth_Service.exception.InvalidCredentialsException;
import com.ezedin.Auth_Service.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

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

    private UserInfo getUserByUsername(String username) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
