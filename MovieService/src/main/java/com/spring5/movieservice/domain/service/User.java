package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userID;
    private String name;
    private String email;
    private String phone;
    private String password; // Only used for registration/login, not returned in responses
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String serviceAddress;
}
