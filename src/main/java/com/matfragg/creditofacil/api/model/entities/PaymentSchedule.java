package com.matfragg.creditofacil.api.model.entities;

import com.matfragg.creditofacil.api.model.enums.PeriodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_schedules")
public class PaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "payment_number", nullable = false)
    private Integer paymentNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "initial_balance", nullable = false)
    private BigDecimal initialBalance;

    private BigDecimal payment;

    private BigDecimal principal;

    private BigDecimal interest;

    @Column(name = "final_balance")
    private BigDecimal finalBalance;

    @Column(name = "life_insurance")
    private BigDecimal lifeInsurance;

    @Column(name = "property_insurance")
    private BigDecimal propertyInsurance;
    
    // ==================== NUEVO CAMPO ====================
    @Column(name = "desgravamen_insurance")
    private BigDecimal desgravamenInsurance;
    // ==================== FIN NUEVO CAMPO ====================

    @Column(name = "total_payment")
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private PeriodType periodType;
}