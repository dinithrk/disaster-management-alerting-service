package com.kernelx.alerts.application.scheduler;

import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;
import com.kernelx.alerts.domain.service.AlertsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final AlertsService alertsService;

    @Scheduled(cron = "${alert.scheduler-cron}")
    public void scheduleAlertGeneration() throws ServerException {
        log.info("Starting scheduled alert generation");

        CreateAlertResponse result = alertsService.createAlertsForTimeWindow();
        log.info("Finished scheduled alert generation: {}", result);
    }
}