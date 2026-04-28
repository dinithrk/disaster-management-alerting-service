package com.kernelx.alerts.domain.model.dto;

import com.kernelx.alerts.domain.enums.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertSeverityDto {

    private AlertSeverity severity;
    private Double breachedThreshold;
}
