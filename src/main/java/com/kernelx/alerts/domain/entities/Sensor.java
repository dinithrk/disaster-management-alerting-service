package com.kernelx.alerts.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    @Id
    @Column(nullable = false)
    private Integer sensorId;
    @Column(nullable = false)
    private Integer sensorTypeId;
    @Column(nullable = false)
    private Integer siteId;
    @Column(nullable = false)
    private Double latitude;
    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false, length = 8)
    private String unitOfMeasure;
    @Column(nullable = false)
    private Double thresholdHighWarning;
    @Column(nullable = false)
    private Double thresholdHighCritical;
    @Column(nullable = false)
    private Double thresholdLowWarning;
    @Column(nullable = false)
    private Double thresholdLowCritical;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorReading> readings = new ArrayList<>();

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    // Optional: Utility methods to keep the Alert relationship in sync
    public void addAlert(Alert alert) {
        alerts.add(alert);
        alert.setSensorId(this.sensorId);
        alert.setSensor(this);
    }

    public void removeAlert(Alert alert) {
        alerts.remove(alert);
        alert.setSensor(null);
    }
}
