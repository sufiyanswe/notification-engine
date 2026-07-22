package com.portfolio.notification.api.controller;

import com.portfolio.notification.api.response.SystemInfoResponse;
import com.portfolio.notification.application.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/v1/system")
public class SystemInfoController {

    private final SystemInfoService systemInfoService;
    public SystemInfoController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @GetMapping("/info")
    public SystemInfoResponse info() {
        return systemInfoService.getSystemInfo();
    }
}
