package com.kernelx.alerts.application.controller;

import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.ActiveAlertResponse;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;
import com.kernelx.alerts.domain.service.AlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${base-url.context}/alerts")
public class AlertsController {

    private final AlertsService alertsService;


    @GetMapping("/create")
    public ResponseEntity<CreateAlertResponse> createAlerts() throws ServerException {
        log.info("Request received to create alert");

        CreateAlertResponse response = alertsService.createAlertsForTimeWindow();
        log.info("Sending response {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ActiveAlertResponse>> getActiveAlerts() throws ServerException {
        log.info("Fetching ACTIVE alerts");

        List<ActiveAlertResponse> response = alertsService.getActiveAlerts();
        log.info("Sending response for {} active alerts", response.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
