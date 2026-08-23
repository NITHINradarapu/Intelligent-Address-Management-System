package com.nithin.addressmanagement.dto;

import com.nithin.addressmanagement.entity.AddressStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AddressResponseDto {

    private Long id;

    private String rawAddress;

    private String houseNumber;
    private String buildingName;
    private String street;
    private String landmark;
    private String area;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private Double confidenceScore;

    private AddressStatus status;
    // Here we are returning only customer ID
    // remember that
    private Long customerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
