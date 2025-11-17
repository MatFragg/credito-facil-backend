package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.BankComparisonResponse;
import com.matfragg.creditofacil.api.dto.response.DashboardStatsResponse;
import com.matfragg.creditofacil.api.dto.response.MonthlyMetricsResponse;
import com.matfragg.creditofacil.api.dto.response.PropertyTrendsResponse;
import com.matfragg.creditofacil.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and analytics endpoints")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get statistics for authenticated client")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getMyStatistics() {
        DashboardStatsResponse stats = dashboardService.getMyStatistics();
        
        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
            .success(true)
            .message("Estadísticas obtenidas exitosamente")
            .data(stats)
            .build());
    }
    
    @GetMapping("/client/{clientId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get statistics for a specific client")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getClientStatistics(@PathVariable Long clientId) {
        DashboardStatsResponse stats = dashboardService.getClientStatistics(clientId);
        
        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
            .success(true)
            .message("Estadísticas del cliente obtenidas exitosamente")
            .data(stats)
            .build());
    }
    
    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get global platform statistics (ADMIN only)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getGlobalStatistics() {
        DashboardStatsResponse stats = dashboardService.getGlobalStatistics();
        
        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
            .success(true)
            .message("Estadísticas globales obtenidas exitosamente")
            .data(stats)
            .build());
    }
    
    @GetMapping("/banks/comparison")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Get bank comparison statistics")
    public ResponseEntity<ApiResponse<List<BankComparisonResponse>>> getBankComparison() {
        List<BankComparisonResponse> comparison = dashboardService.getBankComparison();
        
        return ResponseEntity.ok(ApiResponse.<List<BankComparisonResponse>>builder()
            .success(true)
            .message("Comparación de bancos obtenida exitosamente")
            .data(comparison)
            .build());
    }
    
    @GetMapping("/properties/trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get property market trends")
    public ResponseEntity<ApiResponse<PropertyTrendsResponse>> getPropertyTrends() {
        PropertyTrendsResponse trends = dashboardService.getPropertyTrends();
        
        return ResponseEntity.ok(ApiResponse.<PropertyTrendsResponse>builder()
            .success(true)
            .message("Tendencias del mercado inmobiliario obtenidas exitosamente")
            .data(trends)
            .build());
    }
    
    @GetMapping("/monthly-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get metrics for a specific month (ADMIN only)")
    public ResponseEntity<ApiResponse<MonthlyMetricsResponse>> getMonthlyMetrics(
            @RequestParam int month,
            @RequestParam int year) {
        MonthlyMetricsResponse metrics = dashboardService.getMonthlyMetrics(month, year);
        
        return ResponseEntity.ok(ApiResponse.<MonthlyMetricsResponse>builder()
            .success(true)
            .message(String.format("Métricas de %d/%d obtenidas exitosamente", month, year))
            .data(metrics)
            .build());
    }
}
