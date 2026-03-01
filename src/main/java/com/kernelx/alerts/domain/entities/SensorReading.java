package com.kernelx.alerts.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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

}
