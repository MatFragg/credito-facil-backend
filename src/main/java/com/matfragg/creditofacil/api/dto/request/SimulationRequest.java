package com.matfragg.creditofacil.api.dto.request;

import com.matfragg.creditofacil.api.model.enums.BonusType;
import com.matfragg.creditofacil.api.model.enums.SimulationStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Bank entity ID is required")
    private Long bankEntityId;

    @NotNull(message = "Settings ID is required")
    private Long settingsId;

    @Size(max = 200, message = "Simulation name cannot exceed 200 characters")
    private String simulationName;

    // Currency configuration
    @Pattern(regexp = "^(PEN|USD)$", message = "Currency must be PEN or USD")
    private String currency;

    @Builder.Default
    private Boolean autoConvert = false;

    // Basic parameters
    @NotNull(message = "Property price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal propertyPrice;

    @NotNull(message = "Down payment is required")
    @DecimalMin(value = "0.01", message = "Down payment must be greater than 0")
    private BigDecimal downPayment;

    // Government Bonus (Bono Techo Propio)
    private Boolean applyGovernmentBonus;

    @DecimalMin(value = "0.00", message = "Bonus amount cannot be negative")
    private BigDecimal governmentBonusAmount;

    private BonusType bonusType;

    // Credit parameters
    @NotNull(message = "Annual rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    @DecimalMax(value = "100.00", message = "Rate cannot exceed 100%")
    private BigDecimal annualRate;

    @NotNull(message = "Term in years is required")
    @Min(value = 1, message = "Term must be at least 1 year")
    @Max(value = 30, message = "Term cannot exceed 30 years")
    private Integer termYears;

    // Additional costs
    @DecimalMin(value = "0.0000", message = "Life insurance rate cannot be negative")
    private BigDecimal lifeInsuranceRate;

    @DecimalMin(value = "0.00", message = "Property insurance cannot be negative")
    private BigDecimal propertyInsurance;

    @DecimalMin(value = "0.0000", message = "Desgravamen rate cannot be negative")
    private BigDecimal desgravamenRate;
    
    private Boolean applyPBP;

    @DecimalMin(value = "0.00", message = "Opening commission cannot be negative")
    private BigDecimal openingCommission;

    @DecimalMin(value = "0.00", message = "Notary fees cannot be negative")
    private BigDecimal notaryFees;

    @DecimalMin(value = "0.00", message = "Registration fees cannot be negative")
    private BigDecimal registrationFees;

    private SimulationStatus status;
}
