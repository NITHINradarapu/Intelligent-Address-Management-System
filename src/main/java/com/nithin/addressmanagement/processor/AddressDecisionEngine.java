package com.nithin.addressmanagement.processor;

import com.nithin.addressmanagement.entity.AddressStatus;
import org.springframework.stereotype.Component;

@Component
public class AddressDecisionEngine {

    public DecisionResult decide(
            ValidationResult validationResult,
            double confidenceScore
    ) {

        if (!validationResult.isValid()) {

            return new DecisionResult(
                    AddressStatus.REJECTED,
                    validationResult.getMessage()
            );
        }

        if (confidenceScore >= 90) {

            return new DecisionResult(
                    AddressStatus.APPROVED,
                    "Address successfully parsed and validated"
            );
        }

        if (confidenceScore >= 60) {

            return new DecisionResult(
                    AddressStatus.REVIEW_REQUIRED,
                    "Address requires manual review due to incomplete information"
            );
        }

        return new DecisionResult(
                AddressStatus.REJECTED,
                "Address could not be processed with sufficient confidence"
        );
    }
}