package com.spring5.movieservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@Table("admins")
public class AdminEntity {

    @Id
    private Integer adminId;

    @NotNull(message = "Admin Username is Required")
    private String username;

    @NotNull(message = "Admin Password is Required")
    private String password;

    @NotNull(message = "Admin Email is Required")
    private String email;

    private String role; // ADMIN, SUPER_ADMIN
    private Boolean isActive;
}

