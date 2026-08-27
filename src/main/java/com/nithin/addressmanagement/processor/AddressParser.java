package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddressParser {

    private final LocationLookUp locationLookUp;

    public AddressParser(LocationLookUp locationLookUp){
        this.locationLookUp = locationLookUp;
    }

    private static final Pattern HOUSE_NUMBER_PATTERN =
            Pattern.compile("\\b(?:flat|house|plot|door|no)\\s*(?:no\\s*)?(\\d+[a-zA-Z]?)\\b");

    private static final Pattern POSTAL_CODE_PATTERN =
            Pattern.compile("\\b\\d{6}\\b");

    public ParsedAddress parse(String cleanedAddress) {

        ParsedAddress parsedAddress = new ParsedAddress();

        extractHouseNumber(cleanedAddress, parsedAddress);
        extractPostalCode(cleanedAddress, parsedAddress);
        extractState(cleanedAddress, parsedAddress);
        extractCountry(cleanedAddress, parsedAddress);

        parsedAddress.setCity(
                locationLookUp.findCity(cleanedAddress)
        );

        parsedAddress.setArea(
                locationLookUp.findArea(cleanedAddress)
        );

        extractBuildingName(cleanedAddress, parsedAddress);

        return parsedAddress;
    }

    private void extractHouseNumber(
            String address,
            ParsedAddress parsedAddress
    ) {

        Matcher matcher = HOUSE_NUMBER_PATTERN.matcher(address);

        if (matcher.find()) {
            parsedAddress.setHouseNumber(matcher.group(1));
        }
    }

    private void extractPostalCode(
            String address,
            ParsedAddress parsedAddress
    ) {

        Matcher matcher = POSTAL_CODE_PATTERN.matcher(address);

        if (matcher.find()) {
            parsedAddress.setPostalCode(matcher.group());
        }
    }

    private void extractState(
            String address,
            ParsedAddress parsedAddress
    ) {

        if (address.contains("telangana")) {
            parsedAddress.setState("Telangana");
        } else if (address.contains("andhra pradesh")) {
            parsedAddress.setState("Andhra Pradesh");
        }
    }

    private void extractCountry(
            String address,
            ParsedAddress parsedAddress
    ) {

        if (address.contains("india")) {
            parsedAddress.setCountry("India");
        }
    }

    private void extractBuildingName(
            String address,
            ParsedAddress parsedAddress
    ) {
        String area = locationLookUp.findArea(address);

        if (area == null) {
            return;
        }

        String normalizedArea = area.toLowerCase();

        int areaIndex = address.indexOf(normalizedArea);

        if (areaIndex == -1) {
            return;
        }

        String beforeArea = address.substring(0, areaIndex).trim();

        String houseNumber = parsedAddress.getHouseNumber();

        if (houseNumber != null) {
            int houseNumberIndex = beforeArea.indexOf(houseNumber);

            if (houseNumberIndex != -1) {
                String buildingPart = beforeArea
                        .substring(houseNumberIndex + houseNumber.length())
                        .trim();

                if (!buildingPart.isBlank()) {
                    parsedAddress.setBuildingName(
                            formatText(buildingPart)
                    );
                }
            }
        }
    }
    private String formatText(String text) {

        String[] words = text.split("\\s+");

        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank()) {
                formatted.append(
                        Character.toUpperCase(word.charAt(0))
                );
                formatted.append(word.substring(1).toLowerCase());
                formatted.append(" ");
            }
        }

        return formatted.toString().trim();
    }
}