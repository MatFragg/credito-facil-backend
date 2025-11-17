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
public class BankComparisonResponse {
    
    private Long bankId;
    private String bankName;
    private BigDecimal currentRate;
    private Long totalSimulations;
    private BigDecimal averageFinancedAmount;
    private BigDecimal averageMonthlyPayment;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private BigDecimal averageTCEA;
}
