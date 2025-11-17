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

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDate lastUpdated;
}