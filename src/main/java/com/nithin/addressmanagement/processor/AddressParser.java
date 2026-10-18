package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    private static final Pattern NUMBERED_STREET_PATTERN =
            Pattern.compile("\\b(?:road|street|lane)\\s*(?:no\\s*|number\\s*)?\\d+[a-zA-Z]?\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAMED_STREET_PATTERN =
            Pattern.compile("\\b(?!(?:flat|house|plot|door|residency|apartments?|heights?|villa|enclave|nilayam|near|opposite|opp|behind|beside|next)\\b)(?:100\\s+feet|[a-zA-Z]+|\\d+(?:st|nd|rd|th))\\s+(?:road|street|lane|avenue|marg)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern LANDMARK_CUE_PATTERN =
            Pattern.compile("\\b(near|opposite|opp(?:\\s+to)?|behind|beside|next\\s+to|adjacent\\s+to|close\\s+to)\\s+", Pattern.CASE_INSENSITIVE);

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

        extractStreet(cleanedAddress, parsedAddress);
        extractLandmark(cleanedAddress, parsedAddress);
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

    private void extractStreet(
            String address,
            ParsedAddress parsedAddress
    ) {
        Matcher numberedMatcher = NUMBERED_STREET_PATTERN.matcher(address);
        if (numberedMatcher.find()) {
            parsedAddress.setStreet(formatText(numberedMatcher.group()));
            return;
        }

        Matcher namedMatcher = NAMED_STREET_PATTERN.matcher(address);
        if (namedMatcher.find()) {
            parsedAddress.setStreet(formatText(namedMatcher.group()));
        }
    }

    private void extractLandmark(
            String address,
            ParsedAddress parsedAddress
    ) {
        Matcher matcher = LANDMARK_CUE_PATTERN.matcher(address);
        if (!matcher.find()) {
            return;
        }

        int startIdx = matcher.start();
        int endIdx = address.length();

        List<String> boundaries = new ArrayList<>();
        if (parsedAddress.getStreet() != null) {
            boundaries.add(parsedAddress.getStreet().toLowerCase());
        }
        if (parsedAddress.getArea() != null) {
            boundaries.add(parsedAddress.getArea().toLowerCase());
        }
        if (parsedAddress.getCity() != null) {
            boundaries.add(parsedAddress.getCity().toLowerCase());
        }
        if (parsedAddress.getPostalCode() != null) {
            boundaries.add(parsedAddress.getPostalCode());
        }

        for (String boundary : boundaries) {
            int idx = address.toLowerCase().indexOf(boundary, matcher.end());
            if (idx != -1 && idx < endIdx) {
                endIdx = idx;
            }
        }

        String rawLandmark = address.substring(startIdx, endIdx).trim();
        if (!rawLandmark.isBlank()) {
            parsedAddress.setLandmark(formatText(rawLandmark));
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

                if (parsedAddress.getStreet() != null) {
                    buildingPart = buildingPart
                            .toLowerCase()
                            .replace(parsedAddress.getStreet().toLowerCase(), "")
                            .trim();
                }

                if (parsedAddress.getLandmark() != null) {
                    buildingPart = buildingPart
                            .toLowerCase()
                            .replace(parsedAddress.getLandmark().toLowerCase(), "")
                            .trim();
                }

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