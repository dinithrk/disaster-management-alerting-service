package com.kernelx.alerts.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SensorDataId.class)
public class SensorReading {

    @Id
    @Column(nullable = false)
    private Integer sensorId;
    @Id
    @Column(nullable = false)
    private Instant timestamp;
    @Column(nullable = false)
    private Double measurement;
    @Column
    private Double batteryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensorId", referencedColumnName = "sensorId", insertable = false, updatable = false)
    private Sensor sensor;
}
