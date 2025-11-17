package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationReportResponse {
    
    // Simulation Data
    private Long simulationId;
    private LocalDateTime createdAt;
    
    // Client Information
    private String clientName;
    private String clientDni;
    private String clientPhone;
    private BigDecimal clientMonthlyIncome;
    
    // Property Information
    private String propertyAddress;
    private String propertyCity;
    private String propertyDistrict;
    private BigDecimal propertyPrice;
    private Integer propertyBedrooms;
    private Integer propertyBathrooms;
    private BigDecimal propertyArea;
    
    // Bank Information
    private String bankName;
    private BigDecimal bankCurrentRate;
    
    // Financial Details
    private BigDecimal downPayment;
    private BigDecimal downPaymentPercentage;
    private BigDecimal governmentBonusAmount;
    private BigDecimal amountToFinance;
    private BigDecimal annualRate;
    private Integer termYears;
    private BigDecimal monthlyPayment;
    private BigDecimal lifeInsuranceRate;
    
    // Financial Indicators
    private BigDecimal npv;
    private BigDecimal irr;
    private BigDecimal tcea;
    
    // Payment Schedule
    private List<PaymentScheduleResponse> paymentSchedule;
    
    // Settings
    private String currency;
    private String interestRateType;
    private String capitalization;
    private String gracePeriodType;
    private Integer graceMonths;
}
