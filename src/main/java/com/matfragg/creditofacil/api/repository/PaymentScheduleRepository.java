package com.matfragg.creditofacil.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;


/**
 * Repository interface for managing PaymentSchedule entities.
 * Provides methods to perform CRUD operations and custom queries.
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 * @version 1.0
 */
public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, Long> {
    /**
     * Finds payment schedules by simulation ID, ordered by payment number ascending.
     * @param simulationId
     * @return List of PaymentSchedule entities
     */
    List<PaymentSchedule> findBySimulationIdOrderByPaymentNumberAsc(Long simulationId);

    /**
     * Finds payment schedules by simulation ID.
     * @param simulationId
     * @return List of PaymentSchedule entities
     */
    List<PaymentSchedule> findBySimulationId(Long simulationId);
    
    /**
     * Finds payment schedules with payment dates between the specified start and end dates.
     * @param startDate
     * @param endDate
     * @return List of PaymentSchedule entities
     */
    List<PaymentSchedule> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Counts the number of payment schedules for a given simulation ID.
     * @param simulationId
     * @return The count of PaymentSchedule entities
     */
    long countBySimulationId(Long simulationId);
}