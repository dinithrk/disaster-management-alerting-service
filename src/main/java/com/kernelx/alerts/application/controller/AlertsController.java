package com.kernelx.alerts.application.controller;

import com.kernelx.alerts.domain.service.AlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${base-url.context}/alerts")
public class AlertsController {

    private final AlertsService alertsService;


    @GetMapping("/create")
    public String createAlerts() {
        log.info("Request received to create alert");

        String response = alertsService.createAlertsForTimeWindow();
        log.info("Sending response {}", response);
        return response;
    }
}
