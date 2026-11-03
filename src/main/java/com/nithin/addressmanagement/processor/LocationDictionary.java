package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LocationDictionary implements LocationLookUp {

    private final FuzzyMatcher fuzzyMatcher;

    private static final List<String> CITIES = List.of(
            "hyderabad",
            "bengaluru",
            "bangalore",
            "mumbai",
            "delhi",
            "chennai",
            "pune",
            "kolkata",
            "ahmedabad",
            "jaipur",
            "lucknow",
            "secunderabad",
            "visakhapatnam",
            "vijayawada",
            "warangal"
    );

    private static final List<String> AREAS = List.of(
            "madhapur",
            "gachibowli",
            "hitech city",
            "kondapur",
            "kukatpally",
            "jubilee hills",
            "banjara hills",
            "ameerpet",
            "dilsukhnagar",
            "begumpet",
            "manikonda",
            "kothaguda",
            "miyapur",
            "whitefield",
            "koramangala",
            "indiranagar",
            "electronic city",
            "harlur",
            "bellandur"
    );

    public LocationDictionary(FuzzyMatcher fuzzyMatcher) {
        this.fuzzyMatcher = fuzzyMatcher;
    }

    public LocationDictionary() {
        this.fuzzyMatcher = new FuzzyMatcher();
    }

    @Override
    public String findCity(String address) {
        return findCityMatch(address)
                .map(FuzzyMatchResult::getCanonicalName)
                .orElse(null);
    }

    @Override
    public String findArea(String address) {
        return findAreaMatch(address)
                .map(FuzzyMatchResult::getCanonicalName)
                .orElse(null);
    }

    @Override
    public Optional<FuzzyMatchResult> findCityMatch(String address) {
        Optional<FuzzyMatchResult> match = fuzzyMatcher.findBestMatch(address, CITIES);
        if (match.isPresent()) {
            FuzzyMatchResult res = match.get();
            String formatted = canonicalizeCity(res.getCanonicalName());
            res.setCanonicalName(formatted);
            return Optional.of(res);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FuzzyMatchResult> findAreaMatch(String address) {
        Optional<FuzzyMatchResult> match = fuzzyMatcher.findBestMatch(address, AREAS);
        if (match.isPresent()) {
            FuzzyMatchResult res = match.get();
            res.setCanonicalName(formatLocation(res.getCanonicalName()));
            return Optional.of(res);
        }
        return Optional.empty();
    }

    private String canonicalizeCity(String city) {
        if ("bangalore".equalsIgnoreCase(city)) {
            return "Bengaluru";
        }
        return formatLocation(city);
    }

    private String formatLocation(String location) {
        String[] words = location.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                formatted.append(word.substring(1).toLowerCase());
                formatted.append(" ");
            }
        }
        return formatted.toString().trim();
    }
}
