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
@Table("bookings")
public class BookingEntity {

    @Id
    private Integer bookingId;

    @NotNull(message = "User ID is Required")
    private Integer userId;

    @NotNull(message = "Theater Movie ID is Required")
    private Integer theaterMovieId;

    @NotNull(message = "Number of Seats is Required")
    private Integer numberOfSeats;

    @NotNull(message = "Base Price is Required")
    private Double basePrice; // Price before taxes

    private Double taxAmount; // GST/Tax amount

    private Double serviceCharge; // Service charge

    private Double discountAmount; // Discount if any

    @NotNull(message = "Total Price is Required")
    private Double totalPrice; // Final price including taxes

    @NotNull(message = "Price Per Ticket is Required")
    private Double pricePerTicket;

    private LocalDateTime bookingTime;
    
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    
    private LocalDateTime reservationExpiresAt; // When reservation expires if payment not completed
}


