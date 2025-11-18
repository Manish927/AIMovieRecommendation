package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Integer paymentId;
    private Integer bookingId;
    private Double amount;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET, NET_BANKING
    private String paymentStatus; // PENDING, SUCCESS, FAILED, REFUNDED
    private String transactionId;
    private String gatewayResponse;
    private LocalDateTime paymentDate;
    private Double refundAmount;
    private LocalDateTime refundDate;
    private String refundReason;
    private String serviceAddress;
}

