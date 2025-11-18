package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.JwtUtil;
import com.spring5.movieservice.domain.entity.UserEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.UserRepository;
import com.spring5.movieservice.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;

@RestController
public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            JwtUtil jwtUtil,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService;
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return Mono.error(new InvalidInputException("Email or phone is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Mono.error(new InvalidInputException("Password is required"));
        }

        String emailOrPhone = request.getEmail();
        LOG.info("Login attempt for email/phone: {}", emailOrPhone);

        // Try to find user by email first, then by phone
        Mono<UserEntity> userMono = userRepository.findByEmail(emailOrPhone)
                .switchIfEmpty(userRepository.findByPhone(emailOrPhone));

        return userMono
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid email/phone or password")))
                .flatMap(user -> {
                    // Log password status for debugging
                    LOG.debug("User found: email={}, password null={}, password empty={}, password length={}", 
                            user.getEmail(), 
                            user.getPassword() == null,
                            user.getPassword() != null && user.getPassword().isEmpty(),
                            user.getPassword() != null ? user.getPassword().length() : 0);
                    
                    // Verify password
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        LOG.warn("User password is null or empty for email: {}", user.getEmail());
                        return Mono.error(new InvalidInputException("Invalid email/phone or password"));
                    }
                    
                    boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
                    LOG.debug("Password match result: {}", passwordMatches);
                    
                    if (!passwordMatches) {
                        return Mono.error(new InvalidInputException("Invalid email/phone or password"));
                    }

                    // Check if user is active
                    if (user.getIsActive() != null && !user.getIsActive()) {
                        return Mono.error(new InvalidInputException("User account is inactive"));
                    }

                    // Update last login
                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user)
                            .map(savedUser -> {
                                // Generate JWT token
                                String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getEmail());
                                
                                User userDto = userMapper.entityToAPi(savedUser);
                                
                                LoginResponse response = new LoginResponse();
                                response.setToken(token);
                                response.setUser(userDto);
                                response.setMessage("Login successful");
                                
                                return response;
                            });
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<String> logout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Mono.error(new InvalidInputException("Invalid authorization token"));
        }
        
        String actualToken = token.substring(7);
        jwtUtil.blacklistToken(actualToken);
        
        LOG.info("User logged out successfully");
        return Mono.just("Logout successful");
    }

    @Override
    public Mono<User> register(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return Mono.error(new InvalidInputException("Email is required"));
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Mono.error(new InvalidInputException("Password is required"));
        }

        LOG.info("Registration attempt for email: {}", user.getEmail());

        return userRepository.findByEmail(user.getEmail())
                .flatMap(existing -> Mono.<User>error(new InvalidInputException("Email already registered")))
                .switchIfEmpty(Mono.defer(() -> {
                    // Hash password
                    String hashedPassword = passwordEncoder.encode(user.getPassword());
                    
                    // Generate email verification token
                    String verificationToken = UUID.randomUUID().toString();
                    
                    // Build entity - userId will be auto-generated if null
                    UserEntity.UserEntityBuilder entityBuilder = UserEntity.builder()
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .password(hashedPassword)
                            .emailVerified(false)
                            .phoneVerified(false)
                            .emailVerificationToken(verificationToken)
                            .isActive(true)
                            .createdAt(LocalDateTime.now());
                    
                    // Only set userId if provided (for backward compatibility)
                    if (user.getUserID() != null && user.getUserID() > 0) {
                        entityBuilder.userId(user.getUserID());
                    }
                    
                    UserEntity entity = entityBuilder.build();
                    
                    return userRepository.save(entity)
                            .flatMap(savedUser -> {
                                // Send verification email
                                return emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken)
                                        .then(Mono.just(savedUser));
                            })
                            .map(userMapper::entityToAPi);
                }))
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<String> requestPasswordReset(PasswordResetRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return Mono.error(new InvalidInputException("Email is required"));
        }

        LOG.info("Password reset request for email: {}", request.getEmail());

        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    // Generate reset token
                    String resetToken = UUID.randomUUID().toString();
                    user.setPasswordResetToken(resetToken);
                    user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // 1 hour expiry
                    
                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                // Send password reset email
                                return emailService.sendPasswordResetEmail(savedUser.getEmail(), resetToken)
                                        .then(Mono.just("Password reset email sent successfully"));
                            });
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<String> confirmPasswordReset(PasswordResetConfirm request) {
        if (request.getToken() == null || request.getToken().isEmpty()) {
            return Mono.error(new InvalidInputException("Reset token is required"));
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return Mono.error(new InvalidInputException("New password is required"));
        }

        LOG.info("Password reset confirmation");

        return userRepository.findByPasswordResetToken(request.getToken())
                .switchIfEmpty(Mono.error(new InvalidInputException("Invalid or expired reset token")))
                .flatMap(user -> {
                    // Check if token has expired
                    if (user.getPasswordResetExpires() == null || 
                        user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
                        return Mono.error(new InvalidInputException("Reset token has expired"));
                    }
                    
                    // Update password
                    String hashedPassword = passwordEncoder.encode(request.getNewPassword());
                    user.setPassword(hashedPassword);
                    user.setPasswordResetToken(null);
                    user.setPasswordResetExpires(null);
                    
                    return userRepository.save(user)
                            .then(Mono.just("Password reset successfully"));
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<String> verifyEmail(VerificationRequest request) {
        if (request.getVerificationToken() == null || request.getVerificationToken().isEmpty()) {
            return Mono.error(new InvalidInputException("Verification token is required"));
        }

        LOG.info("Email verification attempt");

        return userRepository.findByEmailVerificationToken(request.getVerificationToken())
                .switchIfEmpty(Mono.error(new InvalidInputException("Invalid verification token")))
                .flatMap(user -> {
                    user.setEmailVerified(true);
                    user.setEmailVerificationToken(null);
                    
                    return userRepository.save(user)
                            .then(Mono.just("Email verified successfully"));
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<String> verifyPhone(VerificationRequest request) {
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            return Mono.error(new InvalidInputException("Phone number is required"));
        }
        if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
            return Mono.error(new InvalidInputException("Verification code is required"));
        }

        LOG.info("Phone verification attempt for: {}", request.getPhone());

        return userRepository.findByPhone(request.getPhone())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    // In production, verify code against stored code and expiry
                    // For demo, we'll accept any 6-digit code
                    if (user.getPhoneVerificationCode() != null && 
                        user.getPhoneVerificationCode().equals(request.getVerificationCode())) {
                        user.setPhoneVerified(true);
                        user.setPhoneVerificationCode(null);
                        
                        return userRepository.save(user)
                                .then(Mono.just("Phone verified successfully"));
                    } else {
                        return Mono.error(new InvalidInputException("Invalid verification code"));
                    }
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<User> validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Mono.error(new InvalidInputException("Invalid authorization token"));
        }
        
        String actualToken = token.substring(7);
        
        if (!jwtUtil.validateToken(actualToken)) {
            return Mono.error(new InvalidInputException("Invalid or expired token"));
        }
        
        String email = jwtUtil.getEmailFromToken(actualToken);
        
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(userMapper::entityToAPi)
                .log(LOG.getName(), Level.FINE);
    }
}

