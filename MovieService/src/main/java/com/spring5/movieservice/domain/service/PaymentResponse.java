package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer paymentId;
    private Integer bookingId;
    private String paymentStatus;
    private String transactionId;
    private String message;
    private Payment payment;
}

