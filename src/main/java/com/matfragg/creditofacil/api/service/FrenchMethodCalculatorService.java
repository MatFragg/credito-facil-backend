package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;
import com.matfragg.creditofacil.api.model.entities.Settings;
import com.matfragg.creditofacil.api.model.enums.Capitalization;
import com.matfragg.creditofacil.api.model.enums.GracePeriodType;
import com.matfragg.creditofacil.api.model.enums.InterestRateType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para cálculo de cronogramas de pago usando el Método Francés
 */
public interface FrenchMethodCalculatorService {
    
    List<PaymentSchedule> generateInitialSchedule(
            BigDecimal amountToFinance, 
            BigDecimal annualRate, 
            Integer termYears, 
            BigDecimal lifeInsuranceRate, 
            BigDecimal propertyInsurance, 
            Capitalization capitalization
    );

    /**
     * Calcula el cronograma completo de pagos
     * @param propertyInsuranceRate Tasa de seguro de riesgo (porcentaje). Si es null, usa propertyInsuranceAmount como monto fijo.
     * @param propertyInsuranceAmount Monto fijo de seguro de riesgo. Solo se usa si propertyInsuranceRate es null.
     */
    List<PaymentSchedule> calculatePaymentSchedule(
            BigDecimal amountToFinance,
            BigDecimal annualRate,
            Integer termYears,
            Settings settings,
            BigDecimal lifeInsurance,
            BigDecimal propertyInsuranceRate,
            BigDecimal propertyInsuranceAmount,
            BigDecimal desgravamenRate
    );

    /**
     * Calcula la cuota mensual fija
     */
    BigDecimal calculateMonthlyPayment(
            BigDecimal amountToFinance,
            BigDecimal monthlyRate,
            Integer totalMonths
    );

    /**
     * Convierte tasa nominal a efectiva si es necesario
     */
    BigDecimal convertToEffectiveRate(
            BigDecimal rate,
            InterestRateType interestRateType,
            Capitalization capitalization
    );

    /**
     * Calcula la TEM (Tasa Efectiva Mensual) a partir de la TEA
     */
    BigDecimal calculateMonthlyRate(BigDecimal effectiveAnnualRate);

    /**
     * Obtiene el número de periodos de capitalización por año
     */
    int getCapitalizationPeriods(Capitalization capitalization);

    /**
     * Aplica lógica de periodo de gracia al cronograma
     */
    // --- CORRECCIÓN: AÑADIR 'lifeInsuranceRate' A LA FIRMA ---
    void applyGracePeriod(
            List<PaymentSchedule> schedule,
            GracePeriodType gracePeriodType,
            Integer graceMonths,
            BigDecimal monthlyRate,
            BigDecimal lifeInsuranceRate // <-- PARÁMETRO AÑADIDO
    );
}