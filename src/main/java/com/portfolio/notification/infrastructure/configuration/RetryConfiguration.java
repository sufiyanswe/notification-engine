package com.portfolio.notification.infrastructure.configuration;

import com.portfolio.notification.application.retry.ExponentialBackoffRetryPolicy;
import com.portfolio.notification.application.retry.RetryPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RetryConfiguration {

    @Bean
    public Clock retryClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RetryPolicy retryPolicy(
            ApplicationProperties applicationProperties,
            Clock retryClock
    ) {

        ApplicationProperties.Retry retry =
                applicationProperties.retry();

        return new ExponentialBackoffRetryPolicy(
                retry.maxRetries(),
                retry.initialDelayMs(),
                retry.multiplier(),
                retryClock
        );
    }
}