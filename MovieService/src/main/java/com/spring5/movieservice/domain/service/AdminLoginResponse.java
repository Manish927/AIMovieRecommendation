package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String token; // Simple token for now (can be enhanced to JWT later)
    private Admin admin;
    private String message;
}

