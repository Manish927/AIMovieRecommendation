package com.spring5.movieservice.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Email Service for sending verification and password reset emails
 * In production, configure SMTP settings in application.properties
 */
@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public Mono<Void> sendVerificationEmail(String email, String token) {
        return Mono.fromRunnable(() -> {
            if (mailSender == null) {
                LOG.warn("Email service not configured. Verification email would be sent to: {}", email);
                LOG.info("Verification token: {}", token);
                LOG.info("Verification link: http://localhost:4200/verify-email?token={}", token);
                return;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Verify Your Email - Movie Booking System");
                message.setText("Please click the following link to verify your email:\n\n" +
                        "http://localhost:4200/verify-email?token=" + token + "\n\n" +
                        "This link will expire in 24 hours.");
                mailSender.send(message);
                LOG.info("Verification email sent to: {}", email);
            } catch (Exception e) {
                LOG.error("Failed to send verification email to: {}", email, e);
            }
        });
    }

    public Mono<Void> sendPasswordResetEmail(String email, String token) {
        return Mono.fromRunnable(() -> {
            if (mailSender == null) {
                LOG.warn("Email service not configured. Password reset email would be sent to: {}", email);
                LOG.info("Password reset token: {}", token);
                LOG.info("Password reset link: http://localhost:4200/reset-password?token={}", token);
                return;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Password Reset Request - Movie Booking System");
                message.setText("You requested a password reset. Click the following link to reset your password:\n\n" +
                        "http://localhost:4200/reset-password?token=" + token + "\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you did not request this, please ignore this email.");
                mailSender.send(message);
                LOG.info("Password reset email sent to: {}", email);
            } catch (Exception e) {
                LOG.error("Failed to send password reset email to: {}", email, e);
            }
        });
    }

    public Mono<Void> sendBookingConfirmationEmail(String email, String bookingDetails) {
        return Mono.fromRunnable(() -> {
            if (mailSender == null) {
                LOG.warn("Email service not configured. Booking confirmation email would be sent to: {}", email);
                LOG.info("Booking details: {}", bookingDetails);
                return;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Booking Confirmation - Movie Booking System");
                message.setText("Your booking has been confirmed!\n\n" + bookingDetails);
                mailSender.send(message);
                LOG.info("Booking confirmation email sent to: {}", email);
            } catch (Exception e) {
                LOG.error("Failed to send booking confirmation email to: {}", email, e);
            }
        });
    }
}

