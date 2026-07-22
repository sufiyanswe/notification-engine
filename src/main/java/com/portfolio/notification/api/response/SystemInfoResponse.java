package com.portfolio.notification.api.response;

import java.time.Instant;

public record SystemInfoResponse(
        String application,
        String version,
        String status,
        Instant timestamp
) {}