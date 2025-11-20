package com.matfragg.creditofacil.api.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para proporcionar información sobre requisitos de cuota inicial
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownPaymentInfoResponse {
    
    private BigDecimal propertyPrice;
    
    private BigDecimal minimumDownPaymentPercentage;
    
    private BigDecimal minimumDownPaymentAmount;
    
    private BigDecimal maximumFinancingPercentage;
    
    private BigDecimal maximumFinancingAmount;
    
    /**
     * Rango de precio: "LOW" (≤ S/ 244,600) o "HIGH" (> S/ 244,600)
     */
    private String priceRange;
    
    /**
     * Nombre de la entidad bancaria
     */
    private String bankEntityName;
}
