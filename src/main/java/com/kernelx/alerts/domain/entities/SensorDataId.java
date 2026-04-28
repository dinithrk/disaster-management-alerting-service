package com.kernelx.alerts.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SensorDataId implements Serializable {

    private Integer sensorId;
    private Instant timestamp;
}
