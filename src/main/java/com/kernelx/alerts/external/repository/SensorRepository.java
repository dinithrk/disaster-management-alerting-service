package com.kernelx.alerts.external.repository;

import com.kernelx.alerts.domain.entities.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Integer> {
}
