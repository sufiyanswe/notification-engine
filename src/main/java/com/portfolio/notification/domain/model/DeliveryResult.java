package com.portfolio.notification.domain.model;

import java.util.Objects;

public record DeliveryResult(

        boolean successful,
        FailureType failureType,
        String reason

) {

    public DeliveryResult {

        Objects.requireNonNull(
                failureType,
                "failureType must not be null."
        );

        if (successful) {

            if (failureType != FailureType.NONE) {
                throw new IllegalArgumentException(
                        "Successful delivery must have failure type NONE."
                );
            }

            if (reason != null) {
                throw new IllegalArgumentException(
                        "Successful delivery must not contain a failure reason."
                );
            }

        } else {

            if (failureType == FailureType.NONE) {
                throw new IllegalArgumentException(
                        "Failed delivery must have a failure type."
                );
            }

            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "Failed delivery must contain a failure reason."
                );
            }
        }
    }

    public static DeliveryResult success() {
        return new DeliveryResult(
                true,
                FailureType.NONE,
                null
        );
    }

    public static DeliveryResult transientFailure(
            String reason
    ) {
        return new DeliveryResult(
                false,
                FailureType.TRANSIENT,
                reason
        );
    }

    public static DeliveryResult permanentFailure(
            String reason
    ) {
        return new DeliveryResult(
                false,
                FailureType.PERMANENT,
                reason
        );
    }
}