package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public PaymentMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public PaymentEntity apiToEntity(Payment api) {
        return PaymentEntity.builder()
                .paymentId(api.getPaymentId())
                .bookingId(api.getBookingId())
                .amount(api.getAmount())
                .paymentMethod(api.getPaymentMethod())
                .paymentStatus(api.getPaymentStatus() != null ? api.getPaymentStatus() : "PENDING")
                .transactionId(api.getTransactionId())
                .gatewayResponse(api.getGatewayResponse())
                .paymentDate(api.getPaymentDate())
                .refundAmount(api.getRefundAmount())
                .refundDate(api.getRefundDate())
                .refundReason(api.getRefundReason())
                .build();
    }

    public Payment entityToApi(PaymentEntity entity) {
        Payment payment = new Payment();
        payment.setPaymentId(entity.getPaymentId());
        payment.setBookingId(entity.getBookingId());
        payment.setAmount(entity.getAmount());
        payment.setPaymentMethod(entity.getPaymentMethod());
        payment.setPaymentStatus(entity.getPaymentStatus());
        payment.setTransactionId(entity.getTransactionId());
        payment.setGatewayResponse(entity.getGatewayResponse());
        payment.setPaymentDate(entity.getPaymentDate());
        payment.setRefundAmount(entity.getRefundAmount());
        payment.setRefundDate(entity.getRefundDate());
        payment.setRefundReason(entity.getRefundReason());
        payment.setServiceAddress(serviceUtil.getServiceAddress());
        return payment;
    }
}

