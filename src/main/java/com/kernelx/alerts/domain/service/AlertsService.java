package com.kernelx.alerts.domain.service;

import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;

public interface AlertsService {

    CreateAlertResponse createAlertsForTimeWindow() throws ServerException;
}
