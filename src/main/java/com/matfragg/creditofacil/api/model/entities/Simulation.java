package com.matfragg.creditofacil.api.model.entities;

import com.matfragg.creditofacil.api.model.enums.BonusType;
import com.matfragg.creditofacil.api.model.enums.SimulationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "simulations")
public class Simulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "bank_entity_id", nullable = false)
    private BankEntity bankEntity;

    @ManyToOne
    @JoinColumn(name = "setting_id", nullable = false)
    private Settings settings;

    // Identification
    @Column(name = "simulation_name")
    private String simulationName;

    @Column(name = "simulation_code", unique = true)
    private String simulationCode;

    // Currency configuration
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "PEN";

    @Column(name = "exchange_rate_used", precision = 10, scale = 4)
    private BigDecimal exchangeRateUsed = BigDecimal.ONE;

    // Basic parameters
    @Column(name = "property_price", nullable = false)
    private BigDecimal propertyPrice;

    @Column(name = "down_payment", nullable = false)
    private BigDecimal downPayment;

    @Column(name = "amount_to_finance", nullable = false)
    private BigDecimal amountToFinance;

    // Monto del préstamo (amountToFinance + gastos iniciales capitalizados)
    @Column(name = "loan_amount", precision = 12, scale = 2)
    private BigDecimal loanAmount;

    // Government Bonus (Bono Techo Propio)
    @Column(name = "apply_government_bonus", nullable = false)
    private Boolean applyGovernmentBonus = false;

    @Column(name = "government_bonus_amount")
    private BigDecimal governmentBonusAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_type")
    private BonusType bonusType;

    @Column(name = "apply_pbp")
    private Boolean applyPBP = false;

    @Column(name = "pbp_amount")
    private BigDecimal pbpAmount = BigDecimal.ZERO;

    // Credit parameters
    @Column(name = "annual_rate", nullable = false)
    private BigDecimal annualRate;

    @Column(name = "term_years", nullable = false)
    private Integer termYears;

    // Additional costs
    @Column(name = "life_insurance_rate")
    private BigDecimal lifeInsuranceRate = new BigDecimal("0.0005");

    @Column(name = "property_insurance")
    private BigDecimal propertyInsurance = new BigDecimal("50.00");
    
    // Tasa de seguro de riesgo (si se usa porcentaje en vez de monto fijo)
    @Column(name = "property_insurance_rate", precision = 8, scale = 6)
    private BigDecimal propertyInsuranceRate;

    @Column(name = "desgravamen_rate", precision = 8, scale = 6)
    private BigDecimal desgravamenRate = new BigDecimal("0.00049");
    
    @Column(name = "total_desgravamen_insurance", precision = 12, scale = 2)
    private BigDecimal totalDesgravamenInsurance = BigDecimal.ZERO;

    // Tasa de descuento para cálculo de VAN (costo de oportunidad)
    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate = BigDecimal.TEN; // Default 10%

    @Column(name = "opening_commission")
    private BigDecimal openingCommission;

    @Column(name = "notary_fees")
    private BigDecimal notaryFees = new BigDecimal("1500.00");

    @Column(name = "registration_fees")
    private BigDecimal registrationFees = new BigDecimal("500.00");

    // Calculated results
    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "total_monthly_payment")
    private BigDecimal totalMonthlyPayment;

    @Column(name = "total_amount_to_pay")
    private BigDecimal totalAmountToPay;

    @Column(name = "total_interest")
    private BigDecimal totalInterest;

    @Column(name = "total_additional_costs")
    private BigDecimal totalAdditionalCosts;

    // Additional calculated fields
    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    @Column(name = "total_life_insurance")
    private BigDecimal totalLifeInsurance;

    @Column(name = "total_property_insurance")
    private BigDecimal totalPropertyInsurance;

    // Financial indicators (MANDATORY SBS)
    @Column(name = "npv", precision = 19, scale = 2)
    private BigDecimal npv;

    @Column(name = "irr", precision = 5, scale = 2)
    private BigDecimal irr;

    @Column(name = "tcea", precision = 5, scale = 2)
    private BigDecimal tcea;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SimulationStatus status = SimulationStatus.DRAFT;

    // Audit
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}