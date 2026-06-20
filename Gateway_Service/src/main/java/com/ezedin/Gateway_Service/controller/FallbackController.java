package com.ezedin.Gateway_Service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        return Mono.just(serviceUnavailable("auth-service"));
    }

    @RequestMapping("/fallback/governance")
    public Mono<ResponseEntity<Map<String, Object>>> governanceFallback() {
        return Mono.just(serviceUnavailable("governance-service"));
    }

    @RequestMapping("/fallback/audit")
    public Mono<ResponseEntity<Map<String, Object>>> auditFallback() {
        return Mono.just(serviceUnavailable("audit-service"));
    }

    private ResponseEntity<Map<String, Object>> serviceUnavailable(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "Service Unavailable",
                "message", service + " is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString()
        ));
    }
}
