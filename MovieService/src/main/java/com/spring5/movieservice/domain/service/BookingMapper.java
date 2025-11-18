package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.BookingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public BookingMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public BookingEntity apiToEntity(Booking api) {
        return BookingEntity.builder()
                .bookingId(api.getBookingId())
                .userId(api.getUserId())
                .theaterMovieId(api.getTheaterMovieId())
                .numberOfSeats(api.getNumberOfSeats())
                .basePrice(api.getBasePrice())
                .taxAmount(api.getTaxAmount())
                .serviceCharge(api.getServiceCharge())
                .discountAmount(api.getDiscountAmount())
                .totalPrice(api.getTotalPrice())
                .pricePerTicket(api.getPricePerTicket())
                .bookingTime(api.getBookingTime())
                .status(api.getStatus() != null ? api.getStatus() : "PENDING")
                .reservationExpiresAt(api.getReservationExpiresAt())
                .build();
    }

    public Booking entityToApi(BookingEntity entity) {
        Booking booking = new Booking();
        booking.setBookingId(entity.getBookingId());
        booking.setUserId(entity.getUserId());
        booking.setTheaterMovieId(entity.getTheaterMovieId());
        booking.setNumberOfSeats(entity.getNumberOfSeats());
        booking.setBasePrice(entity.getBasePrice());
        booking.setTaxAmount(entity.getTaxAmount());
        booking.setServiceCharge(entity.getServiceCharge());
        booking.setDiscountAmount(entity.getDiscountAmount());
        booking.setTotalPrice(entity.getTotalPrice());
        booking.setPricePerTicket(entity.getPricePerTicket());
        booking.setBookingTime(entity.getBookingTime());
        booking.setStatus(entity.getStatus());
        booking.setReservationExpiresAt(entity.getReservationExpiresAt());
        booking.setServiceAddress(serviceUtil.getServiceAddress());
        return booking;
    }
}

