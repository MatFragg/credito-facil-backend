package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.SimulationReportResponse;
import com.matfragg.creditofacil.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation endpoints")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/simulation/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Get detailed report for a simulation")
    public ResponseEntity<ApiResponse<SimulationReportResponse>> getSimulationReport(@PathVariable Long id) {
        SimulationReportResponse report = reportService.getSimulationReport(id);
        
        return ResponseEntity.ok(ApiResponse.<SimulationReportResponse>builder()
            .success(true)
            .message("Reporte de simulación generado exitosamente")
            .data(report)
            .build());
    }
    
    @GetMapping("/simulation/{id}/my-report")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get my simulation report (CLIENT only)")
    public ResponseEntity<ApiResponse<SimulationReportResponse>> getMySimulationReport(@PathVariable Long id) {
        SimulationReportResponse report = reportService.getMySimulationReport(id);
        
        return ResponseEntity.ok(ApiResponse.<SimulationReportResponse>builder()
            .success(true)
            .message("Reporte de simulación generado exitosamente")
            .data(report)
            .build());
    }
    
    @GetMapping("/client/{clientId}/simulations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all simulations report for a client")
    public ResponseEntity<ApiResponse<List<SimulationReportResponse>>> getClientSimulationsReport(
            @PathVariable Long clientId) {
        List<SimulationReportResponse> reports = reportService.getClientSimulationsReport(clientId);
        
        return ResponseEntity.ok(ApiResponse.<List<SimulationReportResponse>>builder()
            .success(true)
            .message("Reporte de simulaciones del cliente generado exitosamente")
            .data(reports)
            .build());
    }
    
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get monthly report with all simulations (ADMIN only)")
    public ResponseEntity<ApiResponse<List<SimulationReportResponse>>> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        List<SimulationReportResponse> reports = reportService.getMonthlyReport(month, year);
        
        return ResponseEntity.ok(ApiResponse.<List<SimulationReportResponse>>builder()
            .success(true)
            .message(String.format("Reporte mensual de %d/%d generado exitosamente", month, year))
            .data(reports)
            .build());
    }
}
