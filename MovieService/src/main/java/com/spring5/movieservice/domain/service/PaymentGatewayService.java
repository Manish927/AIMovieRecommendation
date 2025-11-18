package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Mock Payment Gateway Service
 * In production, this would integrate with actual payment gateways like Razorpay, Stripe, etc.
 */
@Service
public class PaymentGatewayService {

    public Mono<GatewayResponse> processPayment(PaymentRequest request) {
        // Mock payment gateway integration
        // In production, this would call actual payment gateway API
        
        GatewayResponse response = new GatewayResponse();
        
        // Simulate payment processing
        // For demo purposes, we'll simulate success for most cases
        // In production, this would make actual API calls to payment gateway
        
        String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Simulate payment success (90% success rate for demo)
        boolean success = Math.random() > 0.1;
        
        response.setTransactionId(transactionId);
        response.setStatus(success ? "SUCCESS" : "FAILED");
        response.setResponse(success ? 
            "Payment processed successfully" : 
            "Payment failed: Insufficient funds or invalid payment details");
        
        return Mono.just(response);
    }

    public Mono<GatewayResponse> refundPayment(String transactionId, Double amount) {
        // Mock refund processing
        GatewayResponse response = new GatewayResponse();
        
        String refundId = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        response.setTransactionId(refundId);
        response.setStatus("SUCCESS");
        response.setResponse("Refund processed successfully. Amount: " + amount);
        
        return Mono.just(response);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayResponse {
        private String transactionId;
        private String status; // SUCCESS, FAILED
        private String response;
    }
}

