package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AuthService {

    @PostMapping(value = "/auth/login", consumes = "application/json", produces = "application/json")
    Mono<LoginResponse> login(@RequestBody LoginRequest request);

    @PostMapping(value = "/auth/logout", produces = "application/json")
    Mono<String> logout(@RequestHeader("Authorization") String token);

    @PostMapping(value = "/auth/register", consumes = "application/json", produces = "application/json")
    Mono<User> register(@RequestBody User user);

    @PostMapping(value = "/auth/password/reset", consumes = "application/json", produces = "application/json")
    Mono<String> requestPasswordReset(@RequestBody PasswordResetRequest request);

    @PostMapping(value = "/auth/password/reset/confirm", consumes = "application/json", produces = "application/json")
    Mono<String> confirmPasswordReset(@RequestBody PasswordResetConfirm request);

    @PostMapping(value = "/auth/verify/email", consumes = "application/json", produces = "application/json")
    Mono<String> verifyEmail(@RequestBody VerificationRequest request);

    @PostMapping(value = "/auth/verify/phone", consumes = "application/json", produces = "application/json")
    Mono<String> verifyPhone(@RequestBody VerificationRequest request);

    @GetMapping(value = "/auth/validate", produces = "application/json")
    Mono<User> validateToken(@RequestHeader("Authorization") String token);
}

