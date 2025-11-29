package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.BonusType;
import com.matfragg.creditofacil.api.model.enums.SimulationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponse {
    private Long id;
    private Long clientId;
    private Long propertyId;
    private Long bankEntityId;
    private Long settingsId;
    private String simulationName;  // Agregar
    private String simulationCode;  // Agregar si es necesario
    
    // Currency information
    private String currency;
    private String currencySymbol;
    private BigDecimal exchangeRateUsed;
    private BigDecimal propertyPriceAlternate;
    private BigDecimal monthlyPaymentAlternate;
    private String alternateCurrency;
    private String alternateCurrencySymbol;
    
    private BigDecimal propertyPrice;
    private BigDecimal downPayment;
    private BigDecimal amountToFinance;
    private Boolean applyGovernmentBonus;
    private BigDecimal governmentBonusAmount;
    private BonusType bonusType;  // Agregar
    private BigDecimal annualRate;
    private Integer termYears;
    private BigDecimal lifeInsuranceRate;
    private BigDecimal propertyInsurance;
    private BigDecimal desgravamenRate;
    private BigDecimal totalDesgravamenInsurance;
    private Boolean applyPBP;
    private BigDecimal pbpAmount;
    private BigDecimal openingCommission;  // Agregar
    private BigDecimal notaryFees;  // Agregar
    private BigDecimal registrationFees;  // Agregar
    private BigDecimal monthlyPayment;
    private BigDecimal totalMonthlyPayment;  // Agregar si se calcula
    private BigDecimal totalAmountToPay;  // Agregar si se calcula
    private BigDecimal totalInterest;  // Agregar si se calcula
    private BigDecimal totalAdditionalCosts;  // Agregar
    private Integer loanTermMonths;  // Agregar
    private BigDecimal totalLifeInsurance;  // Agregar si se calcula
    private BigDecimal totalPropertyInsurance;  // Agregar si se calcula
    private BigDecimal npv;
    private BigDecimal irr;
    private BigDecimal tcea;
    private SimulationStatus status;  // Agregar
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}