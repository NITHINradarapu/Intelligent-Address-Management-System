package com.nithin.addressmanagement.processor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FuzzyMatcher {

    /**
     * Calculates the Levenshtein edit distance between two strings using
     * dynamic programming with two rows (O(min(m, n)) space complexity).
     */
    public int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null || s1.isEmpty()) return s2 == null ? 0 : s2.length();
        if (s2 == null || s2.isEmpty()) return s1.length();

        String a = s1.toLowerCase();
        String b = s2.toLowerCase();

        int[] previousRow = new int[b.length() + 1];
        int[] currentRow = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            previousRow[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            currentRow[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                currentRow[j] = Math.min(
                        Math.min(currentRow[j - 1] + 1, previousRow[j] + 1),
                        previousRow[j - 1] + cost
                );
            }
            int[] temp = previousRow;
            previousRow = currentRow;
            currentRow = temp;
        }

        return previousRow[b.length()];
    }

    /**
     * Calculates similarity score between 0.0 (completely different) and 1.0 (identical).
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equalsIgnoreCase(s2)) return 1.0;

        int distance = calculateLevenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Searches for the candidate in the address text using both exact substring
     * matching and sliding-window fuzzy matching to catch typos.
     */
    public Optional<FuzzyMatchResult> match(String address, String candidate) {
        if (address == null || candidate == null || address.isBlank() || candidate.isBlank()) {
            return Optional.empty();
        }

        String lowerAddress = address.toLowerCase();
        String lowerCandidate = candidate.toLowerCase();

        // 1. Exact match check
        if (lowerAddress.contains(lowerCandidate)) {
            return Optional.of(new FuzzyMatchResult(
                    candidate,
                    candidate,
                    1.0,
                    0,
                    true
            ));
        }

        // 2. Sliding window fuzzy match
        String[] words = lowerAddress.replaceAll("[^a-z0-9\\s]", " ").trim().split("\\s+");
        String[] candidateWords = lowerCandidate.split("\\s+");
        int windowSize = candidateWords.length;

        if (words.length < windowSize) {
            return Optional.empty();
        }

        double bestSimilarity = 0.0;
        int bestDistance = Integer.MAX_VALUE;
        String bestWindow = null;

        for (int i = 0; i <= words.length - windowSize; i++) {
            StringBuilder windowBuilder = new StringBuilder();
            for (int w = 0; w < windowSize; w++) {
                if (w > 0) windowBuilder.append(" ");
                windowBuilder.append(words[i + w]);
            }
            String window = windowBuilder.toString();

            int distance = calculateLevenshteinDistance(window, lowerCandidate);
            double similarity = calculateSimilarity(window, lowerCandidate);

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestDistance = distance;
                bestWindow = window;
            }
        }

        if (bestWindow != null && isAcceptableMatch(lowerCandidate.length(), bestDistance, bestSimilarity)) {
            return Optional.of(new FuzzyMatchResult(
                    candidate,
                    bestWindow,
                    bestSimilarity,
                    bestDistance,
                    false
            ));
        }

        return Optional.empty();
    }

    /**
     * Finds the best matching candidate from a list of possibilities.
     */
    public Optional<FuzzyMatchResult> findBestMatch(String address, List<String> candidates) {
        if (address == null || candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        FuzzyMatchResult bestResult = null;

        for (String candidate : candidates) {
            Optional<FuzzyMatchResult> matchOpt = match(address, candidate);
            if (matchOpt.isPresent()) {
                FuzzyMatchResult match = matchOpt.get();
                // If exact match found, return immediately
                if (match.isExact()) {
                    return Optional.of(match);
                }
                if (bestResult == null || match.getSimilarity() > bestResult.getSimilarity()) {
                    bestResult = match;
                }
            }
        }

        return Optional.ofNullable(bestResult);
    }

    /**
     * Strict adaptive thresholds based on target length to prevent false positive matches:
     * - Short words (<= 4 chars, e.g. "Pune"): max 1 edit, min 75% similarity
     * - Medium words (5-7 chars, e.g. "Mumbai", "Delhi"): max 2 edits, min 74% similarity
     * - Long words (>= 8 chars, e.g. "Hyderabad", "Banjara Hills"): max 3 edits, min 72% similarity
     */
    private boolean isAcceptableMatch(int targetLength, int distance, double similarity) {
        if (targetLength <= 4) {
            return distance <= 1 && similarity >= 0.75;
        } else if (targetLength <= 7) {
            return distance <= 2 && similarity >= 0.74;
        } else {
            return distance <= 3 && similarity >= 0.72;
        }
    }
}
