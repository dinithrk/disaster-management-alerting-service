package com.kernelx.alerts;

import com.kernelx.alerts.domain.entities.Alert;
import com.kernelx.alerts.domain.entities.Sensor;
import com.kernelx.alerts.domain.entities.SensorReading;
import com.kernelx.alerts.domain.enums.AlertSeverity;
import com.kernelx.alerts.domain.enums.AlertStatus;
import com.kernelx.alerts.external.repository.AlertRepository;
import com.kernelx.alerts.external.repository.SensorReadingRepository;
import com.kernelx.alerts.external.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AlertsServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("kernelx")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        sensorReadingRepository.deleteAll();
        sensorRepository.deleteAll();
    }

    @Test
    void testEndToEndAlertCreationFlow() throws Exception {
        // Pre-populate sensor
        Sensor sensor = new Sensor();
        sensor.setSensorId("sensor-integration-1");
        sensor.setSensorTypeId(1);
        sensor.setSiteId(1);
        sensor.setLatitude(0.0);
        sensor.setLongitude(0.0);
        sensor.setUnitOfMeasure("C");
        sensor.setThresholdHighCritical(90.0);
        sensor.setThresholdHighWarning(80.0);
        sensor.setThresholdLowCritical(10.0);
        sensor.setThresholdLowWarning(20.0);
        sensorRepository.save(sensor);

        // Pre-populate reading that exceeds HighCritical
        SensorReading reading = new SensorReading();
        reading.setSensorId("sensor-integration-1");
        reading.setMeasurement(95.0); // Breaches HighCritical
        reading.setTimestamp(Instant.now());
        sensorReadingRepository.save(reading);

        // Trigger Alert generation via API
        mockMvc.perform(get("/disaster-management/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(1))
                .andExpect(jsonPath("$.resolvedCount").value(0));

        // Verify alert is persisted
        List<Alert> activeAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE);
        assertThat(activeAlerts).hasSize(1);
        Alert activeAlert = activeAlerts.get(0);
        assertThat(activeAlert.getSensorId()).isEqualTo("sensor-integration-1");
        assertThat(activeAlert.getSeverity()).isEqualTo(AlertSeverity.HIGH_CRITICAL);
    }

    @Test
    void testEndToEndActiveAlertsRetrieval() throws Exception {
        // Pre-populate sensor
        Sensor sensor = new Sensor();
        sensor.setSensorId("sensor-integration-2");
        sensor.setSensorTypeId(1);
        sensor.setSiteId(1);
        sensor.setLatitude(0.0);
        sensor.setLongitude(0.0);
        sensor.setUnitOfMeasure("C");
        sensor.setThresholdHighCritical(90.0);
        sensor.setThresholdHighWarning(80.0);
        sensor.setThresholdLowCritical(10.0);
        sensor.setThresholdLowWarning(20.0);
        sensorRepository.save(sensor);

        // Pre-populate alert
        Alert alert = new Alert();
        alert.setAlertId(UUID.randomUUID());
        alert.setSensorId("sensor-integration-2");
        alert.setSeverity(AlertSeverity.HIGH_WARNING);
        alert.setMeasurement(85.0);
        alert.setThreshold(80.0);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTimestamp(Instant.now());
        alert.setFirstCreatedAt(Instant.now());
        alertRepository.save(alert);

        // Trigger Active alerts retrieval via API
        mockMvc.perform(get("/disaster-management/alerts/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value("sensor-integration-2"))
                .andExpect(jsonPath("$[0].severity").value("HIGH_WARNING"))
                .andExpect(jsonPath("$[0].measurement").value(85.0));
    }
}
