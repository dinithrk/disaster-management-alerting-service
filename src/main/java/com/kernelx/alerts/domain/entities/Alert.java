package com.kernelx.alerts.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(nullable = false, length = 32)
    private String alertType; // enum
    @Column(nullable = false, length = 32)
    private String severity; // enum
    @Column(nullable = false)
    private Double measurement;
    @Column(nullable = false)
    private Double threshold;
    @Column(nullable = false, length = 32)
    private String status; // enum

    //todo: convert strings to enums

}
