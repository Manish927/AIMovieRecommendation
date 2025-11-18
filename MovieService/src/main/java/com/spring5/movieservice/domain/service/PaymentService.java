package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {

    @PostMapping(value = "/payments/process", consumes = "application/json", produces = "application/json")
    Mono<PaymentResponse> processPayment(@RequestBody PaymentRequest request);

    @GetMapping(value = "/payments/{paymentId}", produces = "application/json")
    Mono<Payment> getPayment(@PathVariable Integer paymentId);

    @GetMapping(value = "/payments/booking/{bookingId}", produces = "application/json")
    Flux<Payment> getPaymentsByBooking(@PathVariable Integer bookingId);

    @GetMapping(value = "/payments/user/{userId}", produces = "application/json")
    Flux<Payment> getPaymentsByUser(@PathVariable Integer userId);

    @GetMapping(value = "/payments/transaction/{transactionId}", produces = "application/json")
    Mono<Payment> getPaymentByTransactionId(@PathVariable String transactionId);

    @PostMapping(value = "/payments/{paymentId}/refund", consumes = "application/json", produces = "application/json")
    Mono<Payment> refundPayment(@PathVariable Integer paymentId, @RequestBody String reason);
}

