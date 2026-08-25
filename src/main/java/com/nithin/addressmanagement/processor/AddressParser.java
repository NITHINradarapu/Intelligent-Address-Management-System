package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddressParser {

    private final LocationDictionary locationDictionary;

    public AddressParser(LocationDictionary locationDictionary){
        this.locationDictionary = locationDictionary;
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
                locationDictionary.findCity(cleanedAddress)
        );

        parsedAddress.setArea(
                locationDictionary.findArea(cleanedAddress)
        );

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
}