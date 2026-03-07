package com.kernelx.alerts.domain.entities;

import com.kernelx.alerts.domain.enums.AlertSeverity;
import com.kernelx.alerts.domain.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @Column(nullable = false)
    private UUID alertId;
    @Column(nullable = false)
    private Instant timestamp;
    @Column(nullable = false)
    private Integer sensorId;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertSeverity severity;
    @Column(nullable = false)
    private Double measurement;
    @Column(nullable = false)
    private Double threshold;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensorId", referencedColumnName = "sensorId", insertable = false, updatable = false)
    private Sensor sensor;
}
