package com.kernelx.alerts.domain.service.impl;

import com.kernelx.alerts.domain.entities.Alert;
import com.kernelx.alerts.domain.entities.Sensor;
import com.kernelx.alerts.domain.entities.SensorReading;
import com.kernelx.alerts.domain.enums.AlertSeverity;
import com.kernelx.alerts.domain.enums.AlertStatus;
import com.kernelx.alerts.domain.model.dto.AlertSeverityDto;
import com.kernelx.alerts.domain.model.response.CreateAlertResponse;
import com.kernelx.alerts.domain.service.AlertsService;
import com.kernelx.alerts.external.repository.AlertRepository;
import com.kernelx.alerts.external.repository.SensorReadingRepository;
import com.kernelx.alerts.external.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertsServiceImpl implements AlertsService {

    @Value("${alert.time-window-in-mins}")
    private Long timeWindow;

    @Value("${alert.retention-in-days}")
    private Long retentionPeriod;

    private final AlertRepository alertRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public CreateAlertResponse createAlertsForTimeWindow() {
//        Instant now = Instant.now();
        Instant now = Instant.parse("2024-01-01T06:30:00Z"); // 12:00 PM IST
        Instant timeWindowStart = now.minus(timeWindow, ChronoUnit.MINUTES);
        Instant retentionTime = now.minus(retentionPeriod, ChronoUnit.DAYS);

        clearResolvedAlerts(retentionTime);
        List<SensorReading> recentReadings = sensorReadingRepository.findByTimestampBetween(timeWindowStart, now);

        // Group by Sensor ID and get the LATEST reading to determine current state
        Map<Integer, SensorReading> latestReadingsPerSensor = recentReadings.stream()
                .collect(Collectors.toMap(
                        SensorReading::getSensorId,
                        reading -> reading,
                        (existing, replacement) -> existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
                ));

        if (latestReadingsPerSensor.isEmpty()) {
            return new CreateAlertResponse(null, null, "No readings found in the current time window");
        }

        Map<Integer, Sensor> sensorMetadataMap = getSensorMetadata(latestReadingsPerSensor);
        Map<Integer, Alert> activeAlertMap = getCurrentActiveAlerts();

        int createdCount = 0;
        int resolvedCount = 0;

        // Evaluate thresholds
        for (Map.Entry<Integer, SensorReading> entry : latestReadingsPerSensor.entrySet()) {
            Integer sensorId = entry.getKey();
            SensorReading latestReading = entry.getValue();
            Sensor sensorMetadata = sensorMetadataMap.get(sensorId);

            if (sensorMetadata == null) continue;

            Double measurement = latestReading.getMeasurement();
            Alert activeAlert = activeAlertMap.get(sensorId);

            AlertSeverityDto alertSeverityDto = determineMeasurementExceedingThreshold(measurement, sensorMetadata);

            if (alertSeverityDto != null) { // exceeds threshold
                if (activeAlert == null) {
                    // create new alert if non exists
                    Alert newAlert = new Alert(
                            UUID.randomUUID(), now, now, sensorId, alertSeverityDto.getSeverity(),
                            measurement, alertSeverityDto.getBreachedThreshold(), AlertStatus.ACTIVE, null // null for the Sensor relation mapping
                    );
                    alertRepository.save(newAlert);
                    createdCount++;
                } else if (!activeAlert.getSeverity().equals(alertSeverityDto.getSeverity())) {
                    // Escalation/de-escalation logic
                    activeAlert.setSeverity(alertSeverityDto.getSeverity());
                    activeAlert.setMeasurement(measurement);
                    activeAlert.setThreshold(alertSeverityDto.getBreachedThreshold());
                    activeAlert.setTimestamp(now);
                    alertRepository.save(activeAlert);
                }
            } else {
                // Normal state: Resolve if there's an active alert
                if (activeAlert != null) {
                    activeAlert.setStatus(AlertStatus.RESOLVED);
                    activeAlert.setTimestamp(now); // Mark the exact time it was resolved
                    alertRepository.save(activeAlert);
                    resolvedCount++;
                }
            }
        }

        return new CreateAlertResponse(createdCount, resolvedCount, "Alert generation scheduler executed Successfully");
    }

    private void clearResolvedAlerts(Instant retentionTime) {
        alertRepository.deleteByStatusAndTimestampBefore(AlertStatus.RESOLVED, retentionTime);
    }

    private Map<Integer, Sensor> getSensorMetadata(Map<Integer, SensorReading> latestReadingsPerSensor) {
        List<Sensor> sensors = sensorRepository.findAllById(latestReadingsPerSensor.keySet());
        return sensors.stream().collect(Collectors.toMap(Sensor::getSensorId, s -> s));
    }

    private Map<Integer, Alert> getCurrentActiveAlerts() {
        List<Alert> activeAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE);
        return activeAlerts.stream().collect(Collectors.toMap(Alert::getSensorId, a -> a));
    }

    private AlertSeverityDto determineMeasurementExceedingThreshold(Double measurement, Sensor sensorMetadata) {

        AlertSeverityDto alertSeverityDto = null;

        if (measurement >= sensorMetadata.getThresholdHighCritical()) {
            alertSeverityDto = new AlertSeverityDto(AlertSeverity.HIGH_CRITICAL, sensorMetadata.getThresholdHighCritical());
        } else if (measurement >= sensorMetadata.getThresholdHighWarning()) {
            alertSeverityDto = new AlertSeverityDto(AlertSeverity.HIGH_WARNING, sensorMetadata.getThresholdHighWarning());
        } else if (measurement <= sensorMetadata.getThresholdLowCritical()) {
            alertSeverityDto = new AlertSeverityDto(AlertSeverity.LOW_CRITICAL, sensorMetadata.getThresholdLowCritical());
        } else if (measurement <= sensorMetadata.getThresholdLowWarning()) {
            alertSeverityDto = new AlertSeverityDto(AlertSeverity.LOW_WARNING, sensorMetadata.getThresholdLowWarning());
        }

        return alertSeverityDto;
    }
}