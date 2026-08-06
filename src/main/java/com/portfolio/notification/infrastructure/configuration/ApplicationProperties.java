package com.portfolio.notification.infrastructure.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(

        @NotNull
        String name,

        @NotNull
        String version,

        @Valid
        Worker worker

) {

    public record Worker(

            @Min(1)
            int batchSize,

            @Min(100)
            long pollIntervalMs

    ) {
    }
}