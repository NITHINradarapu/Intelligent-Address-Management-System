package com.nithin.addressmanagement.processor;

import java.util.Optional;

public interface LocationLookUp {

    String findCity(String address);

    String findArea(String address);

    default Optional<FuzzyMatchResult> findCityMatch(String address) {
        String city = findCity(address);
        return city == null ? Optional.empty() : Optional.of(new FuzzyMatchResult(city, city, 1.0, 0, true));
    }

    default Optional<FuzzyMatchResult> findAreaMatch(String address) {
        String area = findArea(address);
        return area == null ? Optional.empty() : Optional.of(new FuzzyMatchResult(area, area, 1.0, 0, true));
    }
}
