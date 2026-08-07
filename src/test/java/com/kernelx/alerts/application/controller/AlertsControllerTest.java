package com.kernelx.alerts.application.controller;

import com.kernelx.alerts.domain.enums.AlertSeverity;
import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.ActiveAlertResponse;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;
import com.kernelx.alerts.domain.service.AlertsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlertsController.class, properties = {"base-url.context=/api/v1"})
class AlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertsService alertsService;

    @Test
    void createAlerts_ReturnsOk() throws Exception {
        // Arrange
        CreateAlertResponse response = new CreateAlertResponse(1, 0, "Success");
        when(alertsService.createAlertsForTimeWindow()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(1))
                .andExpect(jsonPath("$.resolvedCount").value(0))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void createAlerts_ServerException_ReturnsServerError() throws Exception {
        // Arrange
        when(alertsService.createAlertsForTimeWindow()).thenThrow(new ServerException("Error generating alerts"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON))
                // Note: Unless a global exception handler is present mapping ServerException, it will result in 500
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getActiveAlerts_ReturnsOk() throws Exception {
        // Arrange
        ActiveAlertResponse alert = new ActiveAlertResponse();
        alert.setAlertId(UUID.randomUUID());
        alert.setSensorId("sensor-1");
        alert.setSeverity(AlertSeverity.HIGH_CRITICAL);
        alert.setMeasurement(95.0);
        alert.setThreshold(90.0);
        alert.setTimestamp(Instant.now());

        when(alertsService.getActiveAlerts()).thenReturn(List.of(alert));

        // Act & Assert
        mockMvc.perform(get("/api/v1/alerts/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value("sensor-1"))
                .andExpect(jsonPath("$[0].severity").value("HIGH_CRITICAL"))
                .andExpect(jsonPath("$[0].measurement").value(95.0))
                .andExpect(jsonPath("$[0].threshold").value(90.0));
    }
}
