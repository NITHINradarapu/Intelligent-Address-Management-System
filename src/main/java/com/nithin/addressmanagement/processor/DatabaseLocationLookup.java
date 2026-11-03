package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.Location;
import com.nithin.addressmanagement.entity.LocationType;
import com.nithin.addressmanagement.repository.LocationRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class DatabaseLocationLookup implements LocationLookUp {

    private final LocationRepository locationRepository;
    private final LocationDictionary locationDictionary;
    private final FuzzyMatcher fuzzyMatcher;

    public DatabaseLocationLookup(
            LocationRepository locationRepository,
            LocationDictionary locationDictionary,
            FuzzyMatcher fuzzyMatcher
    ) {
        this.locationRepository = locationRepository;
        this.locationDictionary = locationDictionary;
        this.fuzzyMatcher = fuzzyMatcher;
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
        Optional<FuzzyMatchResult> dbMatch = findLocationInDb(address, LocationType.CITY);
        if (dbMatch.isPresent()) {
            return dbMatch;
        }
        return locationDictionary.findCityMatch(address);
    }

    @Override
    public Optional<FuzzyMatchResult> findAreaMatch(String address) {
        Optional<FuzzyMatchResult> dbMatch = findLocationInDb(address, LocationType.AREA);
        if (dbMatch.isPresent()) {
            return dbMatch;
        }
        return locationDictionary.findAreaMatch(address);
    }

    private Optional<FuzzyMatchResult> findLocationInDb(String address, LocationType type) {
        List<Location> locations;
        try {
            locations = getLocations(type);
        } catch (Exception e) {
            locations = List.of();
        }

        if (locations == null || locations.isEmpty()) {
            return Optional.empty();
        }

        // 1. Check exact matches first
        for (Location location : locations) {
            String locationName = location.getName().toLowerCase();
            if (address.toLowerCase().contains(locationName)) {
                return Optional.of(new FuzzyMatchResult(
                        location.getName(),
                        location.getName(),
                        1.0,
                        0,
                        true
                ));
            }

            if (location.getAliases() != null) {
                String[] aliases = location.getAliases().split(",");
                for (String alias : aliases) {
                    String normAlias = alias.trim().toLowerCase();
                    if (!normAlias.isEmpty() && address.toLowerCase().contains(normAlias)) {
                        return Optional.of(new FuzzyMatchResult(
                                location.getName(),
                                alias.trim(),
                                1.0,
                                0,
                                true
                        ));
                    }
                }
            }
        }

        // 2. Fuzzy match against DB locations
        List<String> candidateNames = new ArrayList<>();
        for (Location loc : locations) {
            candidateNames.add(loc.getName());
        }

        return fuzzyMatcher.findBestMatch(address, candidateNames);
    }

    @Cacheable("locations")
    public List<Location> getLocations(LocationType type) {
        return locationRepository.findByType(type);
    }
}
