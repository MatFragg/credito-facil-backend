package com.matfragg.creditofacil.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matfragg.creditofacil.api.model.entities.SimulationHistory;

public interface SimulationHistoryRepository extends JpaRepository<SimulationHistory, Long> {
    List<SimulationHistory> findBySimulationId(Long simulationId);

    List<SimulationHistory> findByUserId(Long userId);

    List<SimulationHistory> findByAction(String action);

    List<SimulationHistory> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}