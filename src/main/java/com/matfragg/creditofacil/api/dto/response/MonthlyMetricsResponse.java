package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyMetricsResponse {
    
    private YearMonth month;
    private Long simulationsCount;
    private Long newClientsCount;
    private Long newPropertiesCount;
    private BigDecimal totalFinancedAmount;
    private BigDecimal averagePropertyPrice;
    private BigDecimal averageMonthlyPayment;
}
