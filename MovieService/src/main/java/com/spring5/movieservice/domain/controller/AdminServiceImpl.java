package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.AdminEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.AdminRepository;
import com.spring5.movieservice.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@RestController
public class AdminServiceImpl implements AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final AdminRepository repository;
    private final AdminMapper adminMapper;
    private final ServiceUtil serviceUtil;
    
    // Simple token storage (in production, use Redis or JWT)
    private static final ConcurrentHashMap<String, Admin> tokenStore = new ConcurrentHashMap<>();

    @Autowired
    public AdminServiceImpl(AdminRepository repository, AdminMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.adminMapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<AdminLoginResponse> login(AdminLoginRequest request) {
        LOG.info("Admin login attempt for username: {}", request.getUsername());

        return repository.findByUsernameAndPassword(request.getUsername(), request.getPassword())
                .switchIfEmpty(Mono.error(new InvalidInputException("Invalid username or password")))
                .flatMap(adminEntity -> {
                    if (adminEntity.getIsActive() == null || !adminEntity.getIsActive()) {
                        return Mono.error(new InvalidInputException("Admin account is inactive"));
                    }

                    Admin admin = adminMapper.entityToApi(adminEntity);
                    String token = generateToken(admin);
                    tokenStore.put(token, admin);

                    AdminLoginResponse response = new AdminLoginResponse();
                    response.setToken(token);
                    response.setAdmin(admin);
                    response.setMessage("Login successful");

                    LOG.info("Admin {} logged in successfully", admin.getUsername());
                    return Mono.just(response);
                });
    }

    @Override
    public Mono<Admin> validateToken(String token) {
        Admin admin = tokenStore.get(token);
        if (admin != null) {
            return Mono.just(admin);
        }
        return Mono.error(new InvalidInputException("Invalid or expired token"));
    }

    @Override
    public Mono<Admin> createAdmin(Admin admin) {
        if (admin.getAdminId() != null && admin.getAdminId() < 1) {
            throw new InvalidInputException("Invalid AdminID: " + admin.getAdminId());
        }

        AdminEntity entity = adminMapper.apiToEntity(admin);
        // Note: In production, password should be hashed using BCrypt
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(DuplicateKeyException.class,
                    ex -> new InvalidInputException("Duplicate Key, Admin ID: " + admin.getAdminId()))
                .map(adminMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Admin> getAdmin(Integer adminId) {
        if (adminId < 1) {
            throw new InvalidInputException("Invalid AdminID: " + adminId);
        }

        LOG.info("Getting admin info for ID={}", adminId);

        return repository.findById(adminId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Admin found for Admin ID: " + adminId)))
                .log(LOG.getName(), Level.FINE)
                .map(entity -> adminMapper.entityToApi(entity))
                .map(entity -> setServiceAddress(entity));
    }

    private String generateToken(Admin admin) {
        // Simple token generation (in production, use JWT)
        String tokenData = admin.getAdminId() + ":" + admin.getUsername() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    private Admin setServiceAddress(Admin admin) {
        admin.setServiceAddress(serviceUtil.getServiceAddress());
        return admin;
    }

    // Helper method to check if token is valid (can be used by other services)
    public static boolean isValidToken(String token) {
        return tokenStore.containsKey(token);
    }

    public static Admin getAdminFromToken(String token) {
        return tokenStore.get(token);
    }
}

