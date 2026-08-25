package com.nithin.addressmanagement.processor;


import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationDictionary {

    private static final List<String> CITIES = List.of(
            "hyderabad",
            "bengaluru",
            "mumbai",
            "delhi",
            "chennai",
            "pune"
    );

    private static final List<String> AREAS = List.of(
            "madhapur",
            "gachibowli",
            "hitech city",
            "kondapur",
            "kukatpally",
            "jubilee hills",
            "banjara hills"
    );

    public String findCity(String address) {

        for (String city : CITIES) {
            if (address.contains(city)) {
                return formatLocation(city);
            }
        }

        return null;
    }

    public String findArea(String address) {

        for (String area : AREAS) {
            if (address.contains(area)) {
                return formatLocation(area);
            }
        }

        return null;
    }

    private String formatLocation(String location) {

        String[] words = location.split(" ");

        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            formatted.append(
                    Character.toUpperCase(word.charAt(0))
            );

            formatted.append(word.substring(1));
            formatted.append(" ");
        }

        return formatted.toString().trim();
    }
}
