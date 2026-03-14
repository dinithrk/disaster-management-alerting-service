package com.kernelx.alerts.domain.service;

import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.ActiveAlertResponse;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;

import java.util.List;

public interface AlertsService {

    CreateAlertResponse createAlertsForTimeWindow() throws ServerException;
    List<ActiveAlertResponse> getActiveAlerts() throws ServerException;
}
