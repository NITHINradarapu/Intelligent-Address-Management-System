package com.nithin.addressmanagement.processor;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    private List<String> autoCorrections = new ArrayList<>();

    public void addAutoCorrection(String correction) {
        if (correction != null && !correction.isBlank() && !autoCorrections.contains(correction)) {
            autoCorrections.add(correction);
        }
    }
}
