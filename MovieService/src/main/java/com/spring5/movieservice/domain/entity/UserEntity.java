package com.spring5.movieservice.domain.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Table("users")
public class UserEntity {

    @Id
    private Integer userId;

    @NotNull(message = "User Name is Required")
    private String name;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private Boolean emailVerified;
    
    private Boolean phoneVerified;
    
    private String emailVerificationToken;
    
    private String phoneVerificationCode;
    
    private String passwordResetToken;
    
    private LocalDateTime passwordResetExpires;
    
    private LocalDateTime lastLogin;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
