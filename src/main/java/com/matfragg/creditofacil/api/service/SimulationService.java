package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.SimulationRequest;
import com.matfragg.creditofacil.api.dto.response.PaymentScheduleResponse;
import com.matfragg.creditofacil.api.dto.response.SimulationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SimulationService {

    /**
     * Calcula una simulación sin guardarla (preview)
     */
    SimulationResponse calculate(SimulationRequest request);

    /**
     * Guarda una simulación completa con su cronograma
     */
    SimulationResponse save(SimulationRequest request);

    Page<SimulationResponse> findAll(Pageable pageable);

    SimulationResponse findById(Long id);

    SimulationResponse update(Long id, SimulationRequest request);

    void delete(Long id);

    /**
     * Obtiene las simulaciones del cliente autenticado
     */
    Page<SimulationResponse> getMySimulations(Pageable pageable);

    /**
     * Obtiene el cronograma de pagos de una simulación
     */
    List<PaymentScheduleResponse> getSchedule(Long simulationId);
}
