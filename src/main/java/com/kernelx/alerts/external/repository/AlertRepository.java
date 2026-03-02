package com.kernelx.alerts.external.repository;

import com.kernelx.alerts.domain.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByStatus(String status);

    void deleteByStatusAndTimestampBefore(String status, Instant timestamp);
}
