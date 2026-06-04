package com.ezedin.Auth_Service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Instant expiresAt;
}
