package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private Integer adminId;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private String serviceAddress;
}

