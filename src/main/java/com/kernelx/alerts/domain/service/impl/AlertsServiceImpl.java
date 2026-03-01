package com.kernelx.alerts.domain.service.impl;

import com.kernelx.alerts.domain.service.AlertsService;
import org.springframework.stereotype.Service;

@Service
public class AlertsServiceImpl implements AlertsService {

    @Override
    public String createAlertsForTimeWindow() {
        return "Alert created";
    }
}
