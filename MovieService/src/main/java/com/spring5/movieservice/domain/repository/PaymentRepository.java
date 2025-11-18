package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.PaymentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Integer> {
    Mono<PaymentEntity> findByTransactionId(String transactionId);
    Flux<PaymentEntity> findByBookingId(Integer bookingId);
    Flux<PaymentEntity> findByPaymentStatus(String paymentStatus);
}

