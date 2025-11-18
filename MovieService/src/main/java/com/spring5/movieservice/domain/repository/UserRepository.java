package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Integer> {
    Mono<UserEntity> findByUserId(Integer userId);
    Mono<UserEntity> findByEmail(String email);
    Mono<UserEntity> findByPhone(String phone);
    Mono<UserEntity> findByEmailVerificationToken(String token);
    Mono<UserEntity> findByPasswordResetToken(String token);
    Flux<UserEntity> findAll(); // Added for admin
}
