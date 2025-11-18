package com.spring5.movieservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Table("payments")
public class PaymentEntity {

    @Id
    private Integer paymentId;

    @NotNull(message = "Booking ID is Required")
    private Integer bookingId;

    @NotNull(message = "Amount is Required")
    private Double amount;

    @NotNull(message = "Payment Method is Required")
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, WALLET, NET_BANKING

    private String paymentStatus; // PENDING, SUCCESS, FAILED, REFUNDED

    private String transactionId; // Payment gateway transaction ID

    private String gatewayResponse; // Response from payment gateway

    private LocalDateTime paymentDate;

    private Double refundAmount;

    private LocalDateTime refundDate;

    private String refundReason;
}

