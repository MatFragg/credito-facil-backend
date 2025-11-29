package com.matfragg.creditofacil.api.dto.request;

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
public class BankEntityRequest {

    @NotBlank(message = "El nombre del banco es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotNull(message = "La tasa actual es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa debe ser mayor que 0")
    @DecimalMax(value = "100.00", message = "La tasa no puede exceder 100%")
    private BigDecimal currentRate; // Tasa anual

    @NotNull(message = "El ingreso mínimo es obligatorio")
    @DecimalMin(value = "0.01", message = "El ingreso mínimo debe ser mayor que 0")
    private BigDecimal minimumIncome;

    @NotNull(message = "El porcentaje máximo de cobertura es obligatorio")
    @DecimalMin(value = "0.01", message = "El porcentaje debe ser mayor que 0")
    @DecimalMax(value = "100.00", message = "El porcentaje no puede exceder 100%")
    private BigDecimal maxCoveragePct; // % máximo de financiamiento
    
    @DecimalMin(value = "0.0", message = "La tasa de desgravamen no puede ser negativa")
    private BigDecimal desgravamenRate;
    
    private BigDecimal ncmvMinPropertyValue;
    private BigDecimal ncmvMaxPropertyValue;
    private BigDecimal ncmvMaxPropertyValueCRC;
    
    private BigDecimal pbpThresholdLow;
    private BigDecimal pbpAmountStandard;
    private BigDecimal pbpAmountPlus;
    
    private Boolean supportsNCMV;
}
