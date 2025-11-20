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

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDate lastUpdated;
}