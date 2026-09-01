package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.Location;
import com.nithin.addressmanagement.entity.LocationType;
import com.nithin.addressmanagement.repository.LocationRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class DatabaseLocationLookup implements LocationLookUp {

    private final LocationRepository locationRepository;

    public DatabaseLocationLookup(
            LocationRepository locationRepository
    ) {
        this.locationRepository = locationRepository;
    }

    @Override
    public String findCity(String address) {
        return findLocation(address, LocationType.CITY);
    }

    @Override
    public String findArea(String address) {
        return findLocation(address, LocationType.AREA);
    }

    private String findLocation(
            String address,
            LocationType type
    ) {

        List<Location> locations =
                locationRepository.findByType(type);

        for (Location location : locations) {

            String locationName =
                    location.getName().toLowerCase();

            if (address.contains(locationName)) {
                return location.getName();
            }

            if (location.getAliases() != null) {

                String[] aliases =
                        location.getAliases().split(",");

                for (String alias : aliases) {

                    String normalizedAlias =
                            alias.trim().toLowerCase();

                    if (address.contains(normalizedAlias)) {
                        return location.getName();
                    }
                }
            }
        }

        return null;
    }
}