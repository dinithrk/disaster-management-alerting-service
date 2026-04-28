package com.kernelx.alerts.domain.model.response;

import com.kernelx.alerts.domain.enums.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveAlertResponse {

    private UUID alertId;
    private Instant timestamp;
    private Instant firstCreatedAt;
    private Integer sensorId;
    private AlertSeverity severity;
    private Double measurement;
    private Double threshold;

}
