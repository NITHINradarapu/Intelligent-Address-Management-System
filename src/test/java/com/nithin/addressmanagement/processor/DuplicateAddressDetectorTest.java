package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateAddressDetectorTest {

    private DuplicateAddressDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DuplicateAddressDetector();
    }

    @Test
    void shouldReturnEmptyWhenExistingListIsEmpty() {
        ParsedAddress parsed = new ParsedAddress();
        Optional<Address> result = detector.findDuplicate(Collections.emptyList(), parsed, "Flat 101, Prestige, Bangalore - 560001, India");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDetectDuplicateByCanonicalAddressMatch() {
        Address existing = new Address();
        existing.setId(10L);
        existing.setFormattedAddress("Flat 101, Prestige Shantiniketan, Whitefield, Bangalore, Karnataka - 560066, India");

        ParsedAddress parsed = new ParsedAddress();
        String formatted = "flat 101, prestige shantiniketan, whitefield, bangalore, karnataka - 560066, india";

        Optional<Address> result = detector.findDuplicate(List.of(existing), parsed, formatted);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void shouldDetectDuplicateByStructuralMatchWithSameBuilding() {
        Address existing = new Address();
        existing.setId(20L);
        existing.setPostalCode("560001");
        existing.setHouseNumber("101");
        existing.setBuildingName("Prestige Towers");
        existing.setFormattedAddress("Different formatted representation");

        ParsedAddress parsed = new ParsedAddress();
        parsed.setPostalCode("560001");
        parsed.setHouseNumber("101");
        parsed.setBuildingName("Prestige Towers");

        Optional<Address> result = detector.findDuplicate(List.of(existing), parsed, "Flat 101, Prestige Towers, Bangalore - 560001, India");
        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getId());
    }

    @Test
    void shouldDetectDuplicateByStructuralMatchWithSameStreet() {
        Address existing = new Address();
        existing.setId(30L);
        existing.setPostalCode("560001");
        existing.setHouseNumber("42");
        existing.setStreet("MG Road");
        existing.setFormattedAddress("Old formatted representation");

        ParsedAddress parsed = new ParsedAddress();
        parsed.setPostalCode("560001");
        parsed.setHouseNumber("42");
        parsed.setStreet("mg road");

        Optional<Address> result = detector.findDuplicate(List.of(existing), parsed, "42, MG Road, Bangalore - 560001, India");
        assertTrue(result.isPresent());
        assertEquals(30L, result.get().getId());
    }

    @Test
    void shouldNotDetectDuplicateWhenHouseNumberDiffers() {
        Address existing = new Address();
        existing.setId(40L);
        existing.setPostalCode("560001");
        existing.setHouseNumber("101");
        existing.setBuildingName("Prestige Towers");
        existing.setFormattedAddress("Flat 101, Prestige Towers, Bangalore - 560001, India");

        ParsedAddress parsed = new ParsedAddress();
        parsed.setPostalCode("560001");
        parsed.setHouseNumber("102");
        parsed.setBuildingName("Prestige Towers");

        Optional<Address> result = detector.findDuplicate(List.of(existing), parsed, "Flat 102, Prestige Towers, Bangalore - 560001, India");
        assertTrue(result.isEmpty());
    }
}
