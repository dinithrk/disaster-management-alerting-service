package com.kernelx.alerts.domain.service.impl;

import com.kernelx.alerts.domain.entities.Alert;
import com.kernelx.alerts.domain.entities.Sensor;
import com.kernelx.alerts.domain.entities.SensorReading;
import com.kernelx.alerts.domain.enums.AlertSeverity;
import com.kernelx.alerts.domain.enums.AlertStatus;
import com.kernelx.alerts.domain.exception.ServerException;
import com.kernelx.alerts.domain.model.response.ActiveAlertResponse;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;
import com.kernelx.alerts.external.repository.AlertRepository;
import com.kernelx.alerts.external.repository.SensorReadingRepository;
import com.kernelx.alerts.external.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertsServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private SensorReadingRepository sensorReadingRepository;

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private AlertsServiceImpl alertsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alertsService, "timeWindow", 10L);
        ReflectionTestUtils.setField(alertsService, "retentionPeriod", 7L);
    }

    @Test
    void createAlertsForTimeWindow_NoReadings() throws ServerException {
        // Arrange
        when(sensorReadingRepository.findByTimestampBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // Act
        CreateAlertResponse response = alertsService.createAlertsForTimeWindow();

        // Assert
        assertThat(response.getCreatedCount()).isNull();
        assertThat(response.getResolvedCount()).isNull();
        assertThat(response.getMessage()).isEqualTo("No readings found in the current time window");

        verify(alertRepository, never()).save(any());
    }

    @Test
    void createAlertsForTimeWindow_ThresholdExceeded_CreateNewAlert() throws ServerException {
        // Arrange
        String sensorId = "sensor-1";
        SensorReading reading = new SensorReading();
        reading.setSensorId(sensorId);
        reading.setMeasurement(100.0); // High Critical
        reading.setTimestamp(Instant.now());

        Sensor sensor = new Sensor();
        sensor.setSensorId(sensorId);
        sensor.setThresholdHighCritical(90.0);
        sensor.setThresholdHighWarning(80.0);
        sensor.setThresholdLowCritical(10.0);
        sensor.setThresholdLowWarning(20.0);

        when(sensorReadingRepository.findByTimestampBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(reading));
        when(sensorRepository.findAllById(any())).thenReturn(List.of(sensor));
        when(alertRepository.findByStatus(AlertStatus.ACTIVE)).thenReturn(Collections.emptyList());

        // Act
        CreateAlertResponse response = alertsService.createAlertsForTimeWindow();

        // Assert
        assertThat(response.getCreatedCount()).isEqualTo(1);
        assertThat(response.getResolvedCount()).isZero();

        verify(alertRepository, times(1)).save(argThat(alert ->
                alert.getSensorId().equals(sensorId) &&
                alert.getSeverity() == AlertSeverity.HIGH_CRITICAL &&
                alert.getStatus() == AlertStatus.ACTIVE
        ));
    }

    @Test
    void createAlertsForTimeWindow_ThresholdExceeded_EscalateAlert() throws ServerException {
        // Arrange
        String sensorId = "sensor-1";
        SensorReading reading = new SensorReading();
        reading.setSensorId(sensorId);
        reading.setMeasurement(95.0); // High Critical now
        reading.setTimestamp(Instant.now());

        Sensor sensor = new Sensor();
        sensor.setSensorId(sensorId);
        sensor.setThresholdHighCritical(90.0);
        sensor.setThresholdHighWarning(80.0);
        sensor.setThresholdLowCritical(10.0);
        sensor.setThresholdLowWarning(20.0);

        Alert existingAlert = new Alert();
        existingAlert.setSensorId(sensorId);
        existingAlert.setSeverity(AlertSeverity.HIGH_WARNING);
        existingAlert.setStatus(AlertStatus.ACTIVE);
        existingAlert.setThreshold(80.0);

        when(sensorReadingRepository.findByTimestampBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(reading));
        when(sensorRepository.findAllById(any())).thenReturn(List.of(sensor));
        when(alertRepository.findByStatus(AlertStatus.ACTIVE)).thenReturn(List.of(existingAlert));

        // Act
        CreateAlertResponse response = alertsService.createAlertsForTimeWindow();

        // Assert
        assertThat(response.getCreatedCount()).isZero();
        assertThat(response.getResolvedCount()).isZero();

        verify(alertRepository, times(1)).save(argThat(alert ->
                alert.getSensorId().equals(sensorId) &&
                alert.getSeverity() == AlertSeverity.HIGH_CRITICAL &&
                alert.getThreshold().equals(90.0) &&
                alert.getStatus() == AlertStatus.ACTIVE
        ));
    }

    @Test
    void createAlertsForTimeWindow_NormalState_ResolveAlert() throws ServerException {
        // Arrange
        String sensorId = "sensor-1";
        SensorReading reading = new SensorReading();
        reading.setSensorId(sensorId);
        reading.setMeasurement(50.0); // Normal state
        reading.setTimestamp(Instant.now());

        Sensor sensor = new Sensor();
        sensor.setSensorId(sensorId);
        sensor.setThresholdHighCritical(90.0);
        sensor.setThresholdHighWarning(80.0);
        sensor.setThresholdLowCritical(10.0);
        sensor.setThresholdLowWarning(20.0);

        Alert existingAlert = new Alert();
        existingAlert.setSensorId(sensorId);
        existingAlert.setSeverity(AlertSeverity.HIGH_WARNING);
        existingAlert.setStatus(AlertStatus.ACTIVE);

        when(sensorReadingRepository.findByTimestampBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(reading));
        when(sensorRepository.findAllById(any())).thenReturn(List.of(sensor));
        when(alertRepository.findByStatus(AlertStatus.ACTIVE)).thenReturn(List.of(existingAlert));

        // Act
        CreateAlertResponse response = alertsService.createAlertsForTimeWindow();

        // Assert
        assertThat(response.getCreatedCount()).isZero();
        assertThat(response.getResolvedCount()).isEqualTo(1);

        verify(alertRepository, times(1)).save(argThat(alert ->
                alert.getSensorId().equals(sensorId) &&
                alert.getStatus() == AlertStatus.RESOLVED
        ));
    }

    @Test
    void createAlertsForTimeWindow_ExceptionHandling() {
        // Arrange
        when(sensorReadingRepository.findByTimestampBetween(any(Instant.class), any(Instant.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServerException exception = assertThrows(ServerException.class, () -> {
            alertsService.createAlertsForTimeWindow();
        });

        assertThat(exception.getMessage()).contains("Error occurred while executing Alert scheduler");
    }

    @Test
    void getActiveAlerts_Success() throws ServerException {
        // Arrange
        Alert alert = new Alert();
        alert.setAlertId(UUID.randomUUID());
        alert.setSensorId("sensor-1");
        alert.setSeverity(AlertSeverity.HIGH_CRITICAL);
        alert.setMeasurement(95.0);
        alert.setThreshold(90.0);

        when(alertRepository.findByStatus(AlertStatus.ACTIVE)).thenReturn(List.of(alert));

        // Act
        List<ActiveAlertResponse> response = alertsService.getActiveAlerts();

        // Assert
        assertThat(response).hasSize(1);
        ActiveAlertResponse res = response.get(0);
        assertThat(res.getSensorId()).isEqualTo("sensor-1");
        assertThat(res.getSeverity()).isEqualTo(AlertSeverity.HIGH_CRITICAL);
        assertThat(res.getMeasurement()).isEqualTo(95.0);
        assertThat(res.getThreshold()).isEqualTo(90.0);
    }
}
