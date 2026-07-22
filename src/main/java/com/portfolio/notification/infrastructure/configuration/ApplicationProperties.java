package com.portfolio.notification.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(

        String name,

        String version

) {
}