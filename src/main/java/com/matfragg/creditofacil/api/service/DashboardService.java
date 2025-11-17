package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.response.BankComparisonResponse;
import com.matfragg.creditofacil.api.dto.response.DashboardStatsResponse;
import com.matfragg.creditofacil.api.dto.response.MonthlyMetricsResponse;
import com.matfragg.creditofacil.api.dto.response.PropertyTrendsResponse;

import java.util.List;

public interface DashboardService {
    
    /**
     * Get statistics for a specific client
     */
    DashboardStatsResponse getClientStatistics(Long clientId);
    
    /**
     * Get statistics for authenticated client
     */
    DashboardStatsResponse getMyStatistics();
    
    /**
     * Get global platform statistics (ADMIN only)
     */
    DashboardStatsResponse getGlobalStatistics();
    
    /**
     * Compare banks based on usage and rates
     */
    List<BankComparisonResponse> getBankComparison();
    
    /**
     * Get property market trends
     */
    PropertyTrendsResponse getPropertyTrends();
    
    /**
     * Get metrics for a specific month
     */
    MonthlyMetricsResponse getMonthlyMetrics(int month, int year);
}
