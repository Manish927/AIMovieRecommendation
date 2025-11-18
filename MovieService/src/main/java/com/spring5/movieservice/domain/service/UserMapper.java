package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public UserMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public User entityToAPi(UserEntity entity) {
        User user = new User();
        user.setUserID(entity.getUserId());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setEmailVerified(entity.getEmailVerified());
        user.setPhoneVerified(entity.getPhoneVerified());
        // Don't set password in response for security
        user.setServiceAddress(serviceUtil.getServiceAddress());
        return user;
    }

    public UserEntity apiToEntity(User api) {
        return UserEntity.builder()
                .userId(api.getUserID())
                .name(api.getName())
                .email(api.getEmail())
                .phone(api.getPhone())
                .password(api.getPassword()) // Password will be hashed in service layer
                .emailVerified(api.getEmailVerified())
                .phoneVerified(api.getPhoneVerified())
                .build();
    }
}
