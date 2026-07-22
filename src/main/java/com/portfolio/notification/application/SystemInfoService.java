package com.portfolio.notification.application;
import com.portfolio.notification.api.response.SystemInfoResponse;
import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class SystemInfoService {

    private final ApplicationProperties applicationProperties;
    public SystemInfoService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public SystemInfoResponse getSystemInfo() {
        return new SystemInfoResponse(
                applicationProperties.name(),
                applicationProperties.version(),
                "UP",
                Instant.now()
        );
    }
}
