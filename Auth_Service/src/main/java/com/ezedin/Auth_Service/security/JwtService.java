package com.ezedin.Auth_Service.security;

import com.ezedin.Auth_Service.config.JwtProperties;
import com.ezedin.Auth_Service.dto.UserInfo;
import com.ezedin.Auth_Service.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;

    public String generateToken(UserInfo user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(ROLE_CLAIM, user.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(ROLE_CLAIM, String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject() != null
                    && !claims.getSubject().isBlank()
                    && claims.getExpiration().after(new Date());
        } catch (io.jsonwebtoken.JwtException ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = sha256(keyBytes);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new JwtException("SHA-256 algorithm not available", ex);
        }
    }
}
