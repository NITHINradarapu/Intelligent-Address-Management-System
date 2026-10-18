package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.PostalCode;
import com.nithin.addressmanagement.repository.PostalCodeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressValidator {

    private final PostalCodeRepository postalCodeRepository;

    public AddressValidator(PostalCodeRepository postalCodeRepository) {
        this.postalCodeRepository = postalCodeRepository;
    }

    public ValidationResult validate(ParsedAddress address) {

        ValidationResult postalResult = validatePostalCode(address);

        if (!postalResult.isValid()) {
            return postalResult;
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

    private ValidationResult validatePostalCode(ParsedAddress address) {

        if (address.getPostalCode() == null) {
            return new ValidationResult(
                    false,
                    "Postal code is missing"
            );
        }

        if (!address.getPostalCode().matches("\\d{6}")) {
            return new ValidationResult(
                    false,
                    "Postal code must contain exactly 6 digits"
            );
        }

        Optional<PostalCode> postalCode =
                postalCodeRepository.findByCode(address.getPostalCode());

        if (postalCode.isEmpty()) {
            return new ValidationResult(
                    false,
                    "Postal code was not found in the reference data"
            );
        }

        PostalCode reference = postalCode.get();

        if (address.getCity() != null &&
                !reference.getCity().equalsIgnoreCase(address.getCity())) {

            return new ValidationResult(
                    false,
                    "Postal code does not match the detected city"
            );
        }

        if (address.getState() != null &&
                !reference.getState().equalsIgnoreCase(address.getState())) {

            return new ValidationResult(
                    false,
                    "Postal code does not match the detected state"
            );
        }

        return new ValidationResult(
                true,
                "Postal code validation passed"
        );
    }
}