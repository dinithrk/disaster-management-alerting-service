package com.kernelx.alerts.domain.service.impl;

import com.kernelx.alerts.domain.service.AlertsService;
import com.kernelx.alerts.external.repository.AlertRepository;
import com.kernelx.alerts.external.repository.SensorReadingRepository;
import com.kernelx.alerts.external.repository.SensorRepository;
import com.kernelx.alerts.domain.entities.Alert;
import com.kernelx.alerts.domain.entities.Sensor;
import com.kernelx.alerts.domain.entities.SensorReading;
import lombok.RequiredArgsConstructor;
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

    private final AlertRepository alertRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;

    // Constants to replace your Strings (until you convert them to Enums)
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RESOLVED = "RESOLVED";

    @Override
    @Transactional
    public String createAlertsForTimeWindow() {
//        Instant now = Instant.now();
//        Instant now = Instant.parse("2024-01-01T09:35:00Z");
        Instant now = Instant.parse("2024-01-01T06:30:00Z"); // 12:00 PM IST
        Instant tenMinutesAgo = now.minus(10, ChronoUnit.MINUTES);
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);

        // 1. Cleanup old resolved alerts (keep only 1 day back)
        alertRepository.deleteByStatusAndTimestampBefore(STATUS_RESOLVED, oneDayAgo);

        // 2. Fetch readings in the 10-minute window
        List<SensorReading> recentReadings = sensorReadingRepository.findByTimestampBetween(tenMinutesAgo, now);

        // 3. Group by Sensor ID and get the LATEST reading to determine current state
        Map<Integer, SensorReading> latestReadingsPerSensor = recentReadings.stream()
                .collect(Collectors.toMap(
                        SensorReading::getSensorId,
                        reading -> reading,
                        (existing, replacement) -> existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
                ));

        if (latestReadingsPerSensor.isEmpty()) {
            return "No readings found in the last 10 minutes.";
        }

        // 4. Fetch the associated sensors and current active alerts
        List<Sensor> sensors = sensorRepository.findAllById(latestReadingsPerSensor.keySet());
        Map<Integer, Sensor> sensorMap = sensors.stream().collect(Collectors.toMap(Sensor::getSensorId, s -> s));

        List<Alert> activeAlerts = alertRepository.findByStatus(STATUS_ACTIVE);
        Map<Integer, Alert> activeAlertMap = activeAlerts.stream().collect(Collectors.toMap(Alert::getSensorId, a -> a));

        int createdCount = 0;
        int resolvedCount = 0;

        // 5. Evaluate thresholds
        for (Map.Entry<Integer, SensorReading> entry : latestReadingsPerSensor.entrySet()) {
            Integer sensorId = entry.getKey();
            SensorReading latestReading = entry.getValue();
            Sensor sensor = sensorMap.get(sensorId);

            if (sensor == null) continue;

            Double measurement = latestReading.getMeasurement();
            Alert activeAlert = activeAlertMap.get(sensorId);

            String severity = null;
            Double breachedThreshold = null;

            // Determine if a threshold is currently exceeded
            if (measurement >= sensor.getThresholdHighCritical()) {
                severity = "HIGH_CRITICAL"; breachedThreshold = sensor.getThresholdHighCritical();
            } else if (measurement >= sensor.getThresholdHighWarning()) {
                severity = "HIGH_WARNING"; breachedThreshold = sensor.getThresholdHighWarning();
            } else if (measurement <= sensor.getThresholdLowCritical()) {
                severity = "LOW_CRITICAL"; breachedThreshold = sensor.getThresholdLowCritical();
            } else if (measurement <= sensor.getThresholdLowWarning()) {
                severity = "LOW_WARNING"; breachedThreshold = sensor.getThresholdLowWarning();
            }

            // Execute Alert Logic
            if (severity != null) {
                // Exceeding threshold: Create new alert if none is active
                if (activeAlert == null) {
                    Alert newAlert = new Alert(
                            UUID.randomUUID(), now, sensorId, severity,
                            measurement, breachedThreshold, STATUS_ACTIVE, null // null for the Sensor relation mapping
                    );
                    alertRepository.save(newAlert);
                    createdCount++;
                } else if (!activeAlert.getSeverity().equals(severity)) {
                    // Optional: Escalation logic (e.g., Warning became Critical)
                    activeAlert.setSeverity(severity);
                    activeAlert.setMeasurement(measurement);
                    activeAlert.setThreshold(breachedThreshold);
                    activeAlert.setTimestamp(now);
                    alertRepository.save(activeAlert);
                }
            } else {
                // Normal state: Resolve if there's an active alert
                if (activeAlert != null) {
                    activeAlert.setStatus(STATUS_RESOLVED);
                    activeAlert.setTimestamp(now); // Mark the exact time it was resolved
                    alertRepository.save(activeAlert);
                    resolvedCount++;
                }
            }
        }

        return String.format("Alerts generated: %d created/escalated, %d resolved.", createdCount, resolvedCount);
    }
}