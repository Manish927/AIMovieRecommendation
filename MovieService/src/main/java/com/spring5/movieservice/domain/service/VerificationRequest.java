package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    private String email;
    private String phone;
    private String verificationCode; // For phone verification
    private String verificationToken; // For email verification
}

