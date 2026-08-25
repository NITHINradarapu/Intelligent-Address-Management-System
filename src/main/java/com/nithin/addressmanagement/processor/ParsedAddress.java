package com.nithin.addressmanagement.processor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParsedAddress {
    private String houseNumber;
    private String buildingName;
    private String street;
    private String landmark;
    private String area;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
