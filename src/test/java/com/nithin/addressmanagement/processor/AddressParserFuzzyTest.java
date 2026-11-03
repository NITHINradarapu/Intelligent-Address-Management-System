package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.repository.PostalCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AddressParserFuzzyTest {

    private AddressParser parser;
    private PostalCodeRepository postalCodeRepository;

    @BeforeEach
    void setUp() {
        postalCodeRepository = Mockito.mock(PostalCodeRepository.class);
        Mockito.when(postalCodeRepository.findByCode(Mockito.anyString())).thenReturn(Optional.empty());

        FuzzyMatcher fuzzyMatcher = new FuzzyMatcher();
        LocationDictionary locationDictionary = new LocationDictionary(fuzzyMatcher);
        parser = new AddressParser(locationDictionary, postalCodeRepository, fuzzyMatcher);
    }

    @Test
    void shouldAutoCorrectCityAndAreaAndStateTypos() {
        // "Madhapoor" (typo for Madhapur), "Hydrbad" (typo for Hyderabad), "Telengana" (typo for Telangana)
        String raw = "flat 201, near metro station, madhapoor, hydrbad, telengana 500081, india";

        ParsedAddress parsed = parser.parse(raw);

        assertEquals("201", parsed.getHouseNumber());
        assertEquals("Madhapur", parsed.getArea());
        assertEquals("Hyderabad", parsed.getCity());
        assertEquals("Telangana", parsed.getState());
        assertEquals("500081", parsed.getPostalCode());
        assertEquals("India", parsed.getCountry());

        assertFalse(parsed.getAutoCorrections().isEmpty());
        assertTrue(parsed.getAutoCorrections().stream().anyMatch(c -> c.contains("hydrbad") && c.contains("Hyderabad")));
        assertTrue(parsed.getAutoCorrections().stream().anyMatch(c -> c.contains("madhapoor") && c.contains("Madhapur")));
        assertTrue(parsed.getAutoCorrections().stream().anyMatch(c -> c.contains("telengana") && c.contains("Telangana")));
    }

    @Test
    void shouldAutoCorrectMultiWordAreaTypo() {
        // "banjara hils" (typo for Banjara Hills)
        String raw = "house 55, banjara hils, hyderabad, telangana 500034, india";

        ParsedAddress parsed = parser.parse(raw);

        assertEquals("55", parsed.getHouseNumber());
        assertEquals("Banjara Hills", parsed.getArea());
        assertEquals("Hyderabad", parsed.getCity());
        assertEquals("Telangana", parsed.getState());

        assertTrue(parsed.getAutoCorrections().stream().anyMatch(c -> c.contains("banjara hils") && c.contains("Banjara Hills")));
    }
}
