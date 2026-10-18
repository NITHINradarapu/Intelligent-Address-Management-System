package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddressNormalizer {

    public String normalize(ParsedAddress address) {
        if (address == null) {
            return "";
        }

        List<String> parts = new ArrayList<>();

        if (address.getHouseNumber() != null && !address.getHouseNumber().isBlank()) {
            parts.add(address.getHouseNumber());
        }

        if (address.getBuildingName() != null && !address.getBuildingName().isBlank()) {
            parts.add(address.getBuildingName());
        }

        if (address.getStreet() != null && !address.getStreet().isBlank()) {
            parts.add(address.getStreet());
        }

        if (address.getLandmark() != null && !address.getLandmark().isBlank()) {
            parts.add(address.getLandmark());
        }

        if (address.getArea() != null && !address.getArea().isBlank()) {
            parts.add(address.getArea());
        }

        if (address.getCity() != null && !address.getCity().isBlank()) {
            parts.add(address.getCity());
        }

        if (address.getState() != null && !address.getState().isBlank()) {
            parts.add(address.getState());
        }

        StringBuilder canonical = new StringBuilder(String.join(", ", parts));

        if (address.getPostalCode() != null && !address.getPostalCode().isBlank()) {
            if (!canonical.isEmpty()) {
                canonical.append(" - ");
            }
            canonical.append(address.getPostalCode());
        }

        if (address.getCountry() != null && !address.getCountry().isBlank()) {
            if (!canonical.isEmpty()) {
                canonical.append(", ");
            }
            canonical.append(address.getCountry());
        }

        return canonical.toString();
    }
}
