package com.nithin.addressmanagement.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatcherTest {

    private FuzzyMatcher fuzzyMatcher;

    @BeforeEach
    void setUp() {
        fuzzyMatcher = new FuzzyMatcher();
    }

    @Test
    void testExactMatch() {
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("Flat 101, Road 12, Hyderabad 500081", "hyderabad");
        assertTrue(result.isPresent());
        assertTrue(result.get().isExact());
        assertEquals("hyderabad", result.get().getCanonicalName());
        assertEquals(1.0, result.get().getSimilarity());
    }

    @Test
    void testCityTypoSingleLetterOmission() {
        // "hydrabad" -> "hyderabad" (missing 'e')
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("Flat 101, Road 12, Hydrabad 500081", "hyderabad");
        assertTrue(result.isPresent());
        assertFalse(result.get().isExact());
        assertEquals("hyderabad", result.get().getCanonicalName());
        assertEquals(1, result.get().getDistance());
        assertTrue(result.get().getSimilarity() >= 0.85);
    }

    @Test
    void testCityTypoTwoLettersOmission() {
        // "hydrbad" -> "hyderabad"
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("Flat 101, Road 12, Hydrbad 500081", "hyderabad");
        assertTrue(result.isPresent());
        assertFalse(result.get().isExact());
        assertEquals("hyderabad", result.get().getCanonicalName());
        assertEquals(2, result.get().getDistance());
    }

    @Test
    void testMultiWordAreaTypo() {
        // "banjara hils" -> "banjara hills"
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("Flat 502, Near Park, Banjara Hils, Hyderabad", "banjara hills");
        assertTrue(result.isPresent());
        assertFalse(result.get().isExact());
        assertEquals("banjara hills", result.get().getCanonicalName());
        assertEquals(1, result.get().getDistance());
    }

    @Test
    void testPhoneticVowelTypo() {
        // "madhapoor" -> "madhapur"
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("Flat 301, Near IKEA, Madhapoor, Hyderabad", "madhapur");
        assertTrue(result.isPresent());
        assertEquals("madhapur", result.get().getCanonicalName());

        // "kondapoor" -> "kondapur"
        Optional<FuzzyMatchResult> result2 = fuzzyMatcher.match("Plot 12, Kondapoor, Hyderabad", "kondapur");
        assertTrue(result2.isPresent());
        assertEquals("kondapur", result2.get().getCanonicalName());
    }

    @Test
    void testFindBestMatchFromList() {
        List<String> cities = List.of("hyderabad", "bengaluru", "mumbai", "delhi", "chennai", "pune");
        Optional<FuzzyMatchResult> match = fuzzyMatcher.findBestMatch("Flat 101, near metro, Hydrbad 500081", cities);

        assertTrue(match.isPresent());
        assertEquals("hyderabad", match.get().getCanonicalName());
        assertFalse(match.get().isExact());
    }

    @Test
    void testShouldNotFalsePositiveOnUnrelatedWords() {
        // "phone" shouldn't match "pune"
        Optional<FuzzyMatchResult> result = fuzzyMatcher.match("customer phone number is 9876543210", "pune");
        assertTrue(result.isEmpty());

        // "dal street" shouldn't match "delhi"
        Optional<FuzzyMatchResult> result2 = fuzzyMatcher.match("12 dal street", "delhi");
        assertTrue(result2.isEmpty());
    }
}
