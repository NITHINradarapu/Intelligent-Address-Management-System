package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

@Component
public class ConfidenceCalculator {

    public double calculate(ParsedAddress address) {

        double score = 0;

        if (address.getHouseNumber() != null) {
            score += 10;
        }

        if (address.getBuildingName() != null) {
            score += 15;
        }

        if (address.getArea() != null) {
            score += 15;
        }

        if (address.getCity() != null) {
            score += 15;
        }

        if (address.getState() != null) {
            score += 15;
        }

        if (address.getPostalCode() != null) {
            score += 20;
        }

        if (address.getCountry() != null) {
            score += 10;
        }

        return score;
    }
}