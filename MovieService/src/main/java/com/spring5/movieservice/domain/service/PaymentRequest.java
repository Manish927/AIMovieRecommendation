package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Integer bookingId;
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET, NET_BANKING
    private String paymentDetails; // Card number, UPI ID, Wallet type, etc. (should be tokenized in production)
    private Double amount;
}

