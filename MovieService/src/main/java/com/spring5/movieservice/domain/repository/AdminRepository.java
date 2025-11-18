package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.AdminEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AdminRepository extends ReactiveCrudRepository<AdminEntity, Integer> {
    Mono<AdminEntity> findByUsername(String username);
    Mono<AdminEntity> findByEmail(String email);
    Mono<AdminEntity> findByUsernameAndPassword(String username, String password);
}

