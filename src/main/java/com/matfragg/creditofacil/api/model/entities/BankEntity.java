package com.matfragg.creditofacil.api.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_entities")
public class BankEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "current_rate")
    private BigDecimal currentRate;

    @Column(name = "minimum_income")
    private BigDecimal minimumIncome;

    @Column(name = "max_coverage_pct")
    private BigDecimal maxCoveragePct;

    // Campos para validación de cuota inicial según Fondo MiVivienda
    @Builder.Default
    @Column(name = "min_down_payment_low_range", precision = 5, scale = 2)
    private BigDecimal minDownPaymentLowRange = BigDecimal.valueOf(7.5); // 7.5%

    @Builder.Default
    @Column(name = "min_down_payment_high_range", precision = 5, scale = 2)
    private BigDecimal minDownPaymentHighRange = BigDecimal.valueOf(10.0); // 10%

    @Builder.Default
    @Column(name = "price_threshold", precision = 10, scale = 2)
    private BigDecimal priceThreshold = BigDecimal.valueOf(244600.00); // S/ 244,600

    @Builder.Default
    @Column(name = "max_financing_low_range", precision = 5, scale = 2)
    private BigDecimal maxFinancingLowRange = BigDecimal.valueOf(92.5); // 92.5%

    @Builder.Default
    @Column(name = "max_financing_high_range", precision = 5, scale = 2)
    private BigDecimal maxFinancingHighRange = BigDecimal.valueOf(90.0); // 90%

    // ==================== NUEVOS CAMPOS ====================
    
    // Seguro de Desgravamen (% mensual sobre saldo deudor)
    @Builder.Default
    @Column(name = "desgravamen_rate", precision = 8, scale = 6)
    private BigDecimal desgravamenRate = BigDecimal.valueOf(0.00049); // 0.049% mensual típico
    
    // Rangos de valor de vivienda para NCMV
    @Builder.Default
    @Column(name = "ncmv_min_property_value", precision = 12, scale = 2)
    private BigDecimal ncmvMinPropertyValue = BigDecimal.valueOf(68800.00); // S/ 68,800
    
    @Builder.Default
    @Column(name = "ncmv_max_property_value", precision = 12, scale = 2)
    private BigDecimal ncmvMaxPropertyValue = BigDecimal.valueOf(362100.00); // S/ 362,100
    
    @Builder.Default
    @Column(name = "ncmv_max_property_value_crc", precision = 12, scale = 2)
    private BigDecimal ncmvMaxPropertyValueCRC = BigDecimal.valueOf(488800.00); // S/ 488,800 con CRC
    
    // Rangos para Premio al Buen Pagador (PBP)
    @Builder.Default
    @Column(name = "pbp_threshold_low", precision = 12, scale = 2)
    private BigDecimal pbpThresholdLow = BigDecimal.valueOf(102900.00); // Límite inferior PBP
    
    @Builder.Default
    @Column(name = "pbp_amount_standard", precision = 10, scale = 2)
    private BigDecimal pbpAmountStandard = BigDecimal.valueOf(6400.00); // S/ 6,400
    
    @Builder.Default
    @Column(name = "pbp_amount_plus", precision = 10, scale = 2)
    private BigDecimal pbpAmountPlus = BigDecimal.valueOf(17700.00); // S/ 17,700
    
    // Indica si el banco participa en NCMV
    @Builder.Default
    @Column(name = "supports_ncmv")
    private Boolean supportsNCMV = true;
    
    // ==================== FIN NUEVOS CAMPOS ====================

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDate lastUpdated;
}