package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Theater {
    private Integer theaterId;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private Integer totalScreens;
    private String serviceAddress;

    public Theater() {
        theaterId = 0;
        name = null;
        address = null;
        city = null;
        state = null;
        zipCode = null;
        phone = null;
        totalScreens = 0;
        serviceAddress = null;
    }
}


