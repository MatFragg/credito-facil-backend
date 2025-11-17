package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;
import com.matfragg.creditofacil.api.service.FinancialIndicatorsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
public class FinancialIndicatorsServiceImpl implements FinancialIndicatorsService {

    private static final int SCALE = 10;
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = new BigDecimal("0.10"); // 10% anual

    @Override
    public BigDecimal calculateVAN(
        BigDecimal amountToFinance,
        List<PaymentSchedule> schedule,
        BigDecimal discountRate) { // p. ej. el usuario podría pasar '10' por 10%

        log.debug("Calculando VAN...");

        BigDecimal effectiveDiscountRate;
        if (discountRate == null) {
            // Usa la tasa por defecto (0.10) directamente
            effectiveDiscountRate = DEFAULT_DISCOUNT_RATE;
        } else {
            // Si el usuario pasa un porcentaje (ej: 10), lo convertimos a decimal (0.10)
            effectiveDiscountRate = discountRate.divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
        }

        // Convertir tasa anual (0.10) a mensual
        double monthlyDiscountRate = Math.pow(1 + effectiveDiscountRate.doubleValue(), 1.0 / 12) - 1;

        // VAN comienza con la inversión inicial negativa
        BigDecimal van = amountToFinance.negate();

        // Sumar valor presente de cada flujo
        for (int t = 1; t <= schedule.size(); t++) {
            PaymentSchedule payment = schedule.get(t - 1);
            
            // Esta línea ya la habías corregido, usa el flujo total
            BigDecimal cashFlow = payment.getTotalPayment(); 

            // Valor presente del flujo
            double discountFactor = Math.pow(1 + monthlyDiscountRate, t);
            BigDecimal presentValue = cashFlow.divide(
                    BigDecimal.valueOf(discountFactor),
                    SCALE,
                    RoundingMode.HALF_UP
            );

            van = van.add(presentValue);
        }

        log.debug("VAN calculado: {}", van.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return van.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateTIR(
            BigDecimal amountToFinance,
            List<PaymentSchedule> schedule) {

        log.debug("Calculando TIR mediante método de bisección");

        // TIR es la tasa que hace VAN = 0
        // Usamos método de bisección para encontrarla

        double minRate = 0.0;
        double maxRate = 1.0; // 100%
        double tolerance = 0.0001;
        int maxIterations = 100;
        int iteration = 0;

        while (maxRate - minRate > tolerance && iteration < maxIterations) {
            double midRate = (minRate + maxRate) / 2.0;

            // Calcular VAN con esta tasa
            BigDecimal testVAN = calculateVANWithRate(amountToFinance, schedule, midRate);

            if (testVAN.compareTo(BigDecimal.ZERO) > 0) {
                // VAN positivo, aumentar tasa
                minRate = midRate;
            } else {
                // VAN negativo, disminuir tasa
                maxRate = midRate;
            }

            iteration++;
        }

        double monthlyTIR = (minRate + maxRate) / 2.0;

        // Convertir TIR mensual a anual: TIR_anual = (1 + TIR_mensual)^12 - 1
        double annualTIR = Math.pow(1 + monthlyTIR, 12) - 1;

        BigDecimal tirPercentage = BigDecimal.valueOf(annualTIR * 100)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        log.debug("TIR calculada: {}%", tirPercentage);
        return tirPercentage;
    }

    @Override
    public BigDecimal calculateTCEA(
            BigDecimal amountToFinance,
            List<PaymentSchedule> schedule,
            BigDecimal additionalCosts) {

        log.debug("Calculando TCEA incluyendo todos los costos");

        // TCEA es la TIR considerando todos los costos
        // Flujo inicial = Monto recibido - Costos iniciales
        // Flujos mensuales = Pagos (que ya incluyen seguros)

        BigDecimal initialCashFlow = amountToFinance.subtract(additionalCosts);


        log.debug("Flujo inicial (initialCashFlow): {}", initialCashFlow);
        log.debug("Costos adicionales (additionalCosts): {}", additionalCosts);
        log.debug("Monto financiado (amountToFinance): {}", amountToFinance);
        // Usamos el mismo método de bisección que TIR
        double minRate = 0.0;
        double maxRate = 2.0; // 200% para dar margen
        double tolerance = 0.0001;
        int maxIterations = 100;
        int iteration = 0;

        while (maxRate - minRate > tolerance && iteration < maxIterations) {
            double midRate = (minRate + maxRate) / 2.0;

            // Calcular NPV con esta tasa
            BigDecimal testNPV = calculateNPVForTCEA(initialCashFlow, schedule, midRate);

            if (testNPV.compareTo(BigDecimal.ZERO) > 0) {
                maxRate = midRate; // ✅ CORRECTO
            } else {
                minRate = midRate; // ✅ CORRECTO
            }

            iteration++;

            log.debug("Iteración {}: minRate = {}, maxRate = {}, midRate = {}", iteration, minRate, maxRate, midRate);
            log.debug("NPV calculado con midRate {}: {}", midRate, testNPV);
        }

        double monthlyTCEA = (minRate + maxRate) / 2.0;


        // Convertir a tasa anual
        double annualTCEA = Math.pow(1 + monthlyTCEA, 12) - 1;
        log.debug("TCEA mensual (monthlyTCEA): {}", monthlyTCEA);
        log.debug("TCEA anual (annualTCEA): {}", annualTCEA);
        BigDecimal tceaPercentage = BigDecimal.valueOf(annualTCEA * 100)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        log.debug("TCEA calculada: {}%", tceaPercentage);
        return tceaPercentage;
    }

    // ============ Métodos Privados ============

    private BigDecimal calculateVANWithRate(
            BigDecimal initialInvestment,
            List<PaymentSchedule> schedule,
            double monthlyRate) {

        BigDecimal van = initialInvestment.negate();

        for (int t = 1; t <= schedule.size(); t++) {
            PaymentSchedule payment = schedule.get(t - 1);
            BigDecimal cashFlow = payment.getTotalPayment();

            double discountFactor = Math.pow(1 + monthlyRate, t);
            BigDecimal presentValue = cashFlow.divide(
                    BigDecimal.valueOf(discountFactor),
                    SCALE,
                    RoundingMode.HALF_UP
            );

            van = van.add(presentValue);
        }

        return van;
    }

    private BigDecimal calculateNPVForTCEA(
            BigDecimal initialCashFlow,
            List<PaymentSchedule> schedule,
            double monthlyRate) {

        // NPV = Flujo_0 + Σ(-Pago_t / (1 + r)^t)
        // Flujo_0 es positivo (dinero recibido)
        // Pagos son negativos (dinero que se paga)

        BigDecimal npv = initialCashFlow;

        for (int t = 1; t <= schedule.size(); t++) {
            PaymentSchedule payment = schedule.get(t - 1);
            
            BigDecimal cashFlow = payment.getTotalPayment().negate();

            double discountFactor = Math.pow(1 + monthlyRate, t);
            BigDecimal presentValue = cashFlow.divide(
                    BigDecimal.valueOf(discountFactor),
                    SCALE,
                    RoundingMode.HALF_UP
            );

            npv = npv.add(presentValue);
        }

        return npv;
    }
}
