package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.response.SimulationReportResponse;

import java.util.List;

public interface ReportService {
    
    /**
     * Generate detailed report for a single simulation
     */
    SimulationReportResponse getSimulationReport(Long simulationId);
    
    /**
     * Generate report with all simulations for a client
     */
    List<SimulationReportResponse> getClientSimulationsReport(Long clientId);
    
    /**
     * Generate monthly report with all simulations in a given month
     * Restricted to ADMIN role
     */
    List<SimulationReportResponse> getMonthlyReport(int month, int year);
    
    /**
     * Get simulation report for authenticated user's simulation
     */
    SimulationReportResponse getMySimulationReport(Long simulationId);
}
