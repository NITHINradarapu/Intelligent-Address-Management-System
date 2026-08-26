package com.nithin.addressmanagement.processor;

import lombok.Getter;

@Getter
public class ValidationResult {

    private final boolean valid;
    private final String message;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}