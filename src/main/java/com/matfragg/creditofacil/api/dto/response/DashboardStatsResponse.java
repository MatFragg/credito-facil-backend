package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    
    private Long totalSimulations;
    private Long totalClients;
    private Long totalProperties;
    private BigDecimal totalFinancedAmount;
    private BigDecimal averageMonthlyPayment;
    private BigDecimal averagePropertyPrice;
    private BigDecimal averageDownPayment;
    private Integer averageLoanYears;
    private String mostUsedBank;
    private String mostPopularPropertyType;
    private Long simulationsThisMonth;
    private Long simulationsThisYear;
}
