package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.PaymentEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.BookingRepository;
import com.spring5.movieservice.domain.repository.PaymentRepository;
import com.spring5.movieservice.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;

@RestController
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            PaymentMapper paymentMapper,
            PaymentGatewayService paymentGatewayService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.paymentMapper = paymentMapper;
        this.paymentGatewayService = paymentGatewayService;
    }

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        if (request.getBookingId() == null || request.getBookingId() < 1) {
            return Mono.error(new InvalidInputException("Invalid Booking ID"));
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return Mono.error(new InvalidInputException("Invalid payment amount"));
        }

        LOG.info("Processing payment for booking ID: {}", request.getBookingId());

        return bookingRepository.findById(request.getBookingId())
                .switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + request.getBookingId())))
                .flatMap(booking -> {
                    // Verify booking amount matches payment amount
                    if (!booking.getTotalPrice().equals(request.getAmount())) {
                        return Mono.error(new InvalidInputException("Payment amount does not match booking total"));
                    }

                    // Check if booking is in PENDING status
                    if (!"PENDING".equals(booking.getStatus())) {
                        return Mono.error(new InvalidInputException("Booking is not in PENDING status"));
                    }

                    // Create payment entity
                    PaymentEntity paymentEntity = PaymentEntity.builder()
                            .bookingId(request.getBookingId())
                            .amount(request.getAmount())
                            .paymentMethod(request.getPaymentMethod())
                            .paymentStatus("PENDING")
                            .paymentDate(LocalDateTime.now())
                            .build();

                    // Process payment through gateway
                    return paymentGatewayService.processPayment(request)
                            .flatMap(gatewayResponse -> {
                                paymentEntity.setTransactionId(gatewayResponse.getTransactionId());
                                paymentEntity.setPaymentStatus(gatewayResponse.getStatus());
                                paymentEntity.setGatewayResponse(gatewayResponse.getResponse());

                                return paymentRepository.save(paymentEntity)
                                        .flatMap(savedPayment -> {
                                            // Update booking status based on payment result
                                            if ("SUCCESS".equals(gatewayResponse.getStatus())) {
                                                booking.setStatus("CONFIRMED");
                                                return bookingRepository.save(booking)
                                                        .then(Mono.just(savedPayment));
                                            } else {
                                                // Payment failed, keep booking as PENDING
                                                return Mono.just(savedPayment);
                                            }
                                        });
                            })
                            .map(paymentMapper::entityToApi)
                            .map(payment -> {
                                PaymentResponse response = new PaymentResponse();
                                response.setPaymentId(payment.getPaymentId());
                                response.setBookingId(payment.getBookingId());
                                response.setPaymentStatus(payment.getPaymentStatus());
                                response.setTransactionId(payment.getTransactionId());
                                response.setPayment(payment);
                                
                                if ("SUCCESS".equals(payment.getPaymentStatus())) {
                                    response.setMessage("Payment processed successfully");
                                } else {
                                    response.setMessage("Payment processing failed");
                                }
                                
                                return response;
                            });
                })
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<Payment> getPayment(Integer paymentId) {
        if (paymentId < 1) {
            throw new InvalidInputException("Invalid Payment ID: " + paymentId);
        }
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found: " + paymentId)))
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Flux<Payment> getPaymentsByBooking(Integer bookingId) {
        if (bookingId < 1) {
            return Flux.error(new InvalidInputException("Invalid Booking ID: " + bookingId));
        }
        return paymentRepository.findByBookingId(bookingId)
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Flux<Payment> getPaymentsByUser(Integer userId) {
        if (userId < 1) {
            return Flux.error(new InvalidInputException("Invalid User ID: " + userId));
        }
        // Get all bookings for user, then get payments for those bookings
        return bookingRepository.findByUserId(userId)
                .flatMap(booking -> paymentRepository.findByBookingId(booking.getBookingId()))
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Mono<Payment> getPaymentByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            return Mono.error(new InvalidInputException("Invalid Transaction ID"));
        }
        return paymentRepository.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found for transaction: " + transactionId)))
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Mono<Payment> refundPayment(Integer paymentId, String reason) {
        if (paymentId < 1) {
            return Mono.error(new InvalidInputException("Invalid Payment ID: " + paymentId));
        }
        
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found: " + paymentId)))
                .flatMap(payment -> {
                    if (!"SUCCESS".equals(payment.getPaymentStatus())) {
                        return Mono.error(new InvalidInputException("Only successful payments can be refunded"));
                    }
                    
                    // Process refund through gateway
                    return paymentGatewayService.refundPayment(payment.getTransactionId(), payment.getAmount())
                            .flatMap(refundResponse -> {
                                payment.setPaymentStatus("REFUNDED");
                                payment.setRefundAmount(payment.getAmount());
                                payment.setRefundDate(LocalDateTime.now());
                                payment.setRefundReason(reason);
                                
                                return paymentRepository.save(payment)
                                        .flatMap(refundedPayment -> {
                                            // Update booking status to CANCELLED
                                            return bookingRepository.findById(payment.getBookingId())
                                                    .flatMap(booking -> {
                                                        booking.setStatus("CANCELLED");
                                                        return bookingRepository.save(booking)
                                                                .then(Mono.just(refundedPayment));
                                                    });
                                        });
                            })
                            .map(paymentMapper::entityToApi);
                });
    }
}

