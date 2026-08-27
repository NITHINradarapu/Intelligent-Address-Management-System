package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.AddressStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DecisionResult {

    private AddressStatus status;
    private String message;
}