package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.AdminEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public AdminMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public AdminEntity apiToEntity(Admin api) {
        return AdminEntity.builder()
                .adminId(api.getAdminId())
                .username(api.getUsername())
                .email(api.getEmail())
                .role(api.getRole() != null ? api.getRole() : "ADMIN")
                .isActive(api.getIsActive() != null ? api.getIsActive() : true)
                .build();
    }

    public Admin entityToApi(AdminEntity entity) {
        Admin admin = new Admin();
        admin.setAdminId(entity.getAdminId());
        admin.setUsername(entity.getUsername());
        admin.setEmail(entity.getEmail());
        admin.setRole(entity.getRole());
        admin.setIsActive(entity.getIsActive());
        admin.setServiceAddress(serviceUtil.getServiceAddress());
        return admin;
    }
}

