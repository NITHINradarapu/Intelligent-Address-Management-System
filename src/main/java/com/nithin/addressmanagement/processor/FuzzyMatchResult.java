package com.nithin.addressmanagement.processor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuzzyMatchResult {

    private String canonicalName;
    private String matchedText;
    private double similarity;
    private int distance;
    private boolean exact;

    public String getCorrectionSummary() {
        if (exact) {
            return null;
        }
        int percent = (int) Math.round(similarity * 100);
        return String.format("'%s' -> '%s' (%d%% match)", matchedText, canonicalName, percent);
    }
}
