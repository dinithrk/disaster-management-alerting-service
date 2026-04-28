package com.kernelx.alerts.external.repository;

import com.kernelx.alerts.domain.entities.SensorDataId;
import com.kernelx.alerts.domain.entities.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, SensorDataId> {

    List<SensorReading> findByTimestampBetween(Instant start, Instant end);
}
