package com.portfolio.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/v1/system")
public class SystemInfoController {

    @GetMapping("/info")
    public String info() {
        return "Notification Engine is running";
    }
}
