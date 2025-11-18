package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AdminService {

    @PostMapping(value = "/admin/login", consumes = "application/json", produces = "application/json")
    Mono<AdminLoginResponse> login(@RequestBody AdminLoginRequest request);

    @GetMapping(value = "/admin/validate/{token}", produces = "application/json")
    Mono<Admin> validateToken(@PathVariable String token);

    @PostMapping(value = "/admin", consumes = "application/json", produces = "application/json")
    Mono<Admin> createAdmin(@RequestBody Admin admin);

    @GetMapping(value = "/admin/{adminId}", produces = "application/json")
    Mono<Admin> getAdmin(@PathVariable Integer adminId);
}

