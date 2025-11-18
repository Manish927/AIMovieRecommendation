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
@Table("theaters")
public class TheaterEntity {

    @Id
    private Integer theaterId;

    @NotNull(message = "Theater Name is Required")
    private String name;

    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private Integer totalScreens;
}


