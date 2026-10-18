package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DuplicateAddressDetector {

    public Optional<Address> findDuplicate(
            List<Address> existingAddresses,
            ParsedAddress parsedAddress,
            String formattedAddress
    ) {
        if (existingAddresses == null || existingAddresses.isEmpty()) {
            return Optional.empty();
        }

        for (Address existing : existingAddresses) {
            // Check 1: Exact canonical formatted address match
            if (formattedAddress != null && !formattedAddress.isBlank()
                    && matches(existing.getFormattedAddress(), formattedAddress)) {
                return Optional.of(existing);
            }

            // Check 2: Structural match (Postal Code + House Number + (Building Name OR Street))
            boolean postalMatch = matches(existing.getPostalCode(), parsedAddress.getPostalCode());
            boolean houseMatch = matches(existing.getHouseNumber(), parsedAddress.getHouseNumber());
            boolean buildingMatch = matches(existing.getBuildingName(), parsedAddress.getBuildingName());
            boolean streetMatch = matches(existing.getStreet(), parsedAddress.getStreet());

            if (postalMatch && houseMatch && (buildingMatch || streetMatch)) {
                return Optional.of(existing);
            }
        }

        return Optional.empty();
    }

    private boolean matches(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        String t1 = s1.trim();
        String t2 = s2.trim();
        if (t1.isEmpty() || t2.isEmpty()) {
            return false;
        }
        return t1.equalsIgnoreCase(t2);
    }
}
