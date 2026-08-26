package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

@Component
public class AddressValidator {

    public ValidationResult validate(ParsedAddress address) {

        if (address.getPostalCode() != null &&
                !address.getPostalCode().matches("\\d{6}")) {

            return new ValidationResult(
                    false,
                    "Postal code must contain exactly 6 digits"
            );
        }

        if (address.getCountry() != null &&
                address.getCountry().equalsIgnoreCase("India") &&
                address.getPostalCode() == null) {

            return new ValidationResult(
                    false,
                    "Indian address should contain a postal code"
            );
        }

        if (address.getCity() != null &&
                address.getState() == null) {

            return new ValidationResult(
                    false,
                    "City detected but state is missing"
            );
        }

        return new ValidationResult(
                true,
                "Address validation passed"
        );
    }
}