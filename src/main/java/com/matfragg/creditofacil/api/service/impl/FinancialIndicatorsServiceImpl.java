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

    private static final int SCALE = 15; // Mayor precisión para cálculos intermedios
    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 5; // Para tasas en porcentaje (ej: 14.72334%)
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = new BigDecimal("0.10"); // 10% anual

    @Override
    public BigDecimal calculateVAN(
        BigDecimal amountToFinance,
        List<PaymentSchedule> schedule,
        BigDecimal discountRate) {

        log.debug("Calculando VAN...");

        BigDecimal effectiveDiscountRate;
        if (discountRate == null) {
            effectiveDiscountRate = DEFAULT_DISCOUNT_RATE;
        } else {
            effectiveDiscountRate = discountRate.divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP);
        }

        // Convertir tasa anual a mensual: (1 + TEA)^(1/12) - 1
        double monthlyDiscountRate = Math.pow(1 + effectiveDiscountRate.doubleValue(), 1.0 / 12.0) - 1;

        // VAN desde perspectiva del PRESTATARIO:
        // Flujo_0 = +Préstamo (dinero recibido)
        // Flujos 1..n = -Pagos (dinero que sale)
        // VAN = Préstamo - VP(Pagos)
        // Si VAN > 0: El préstamo es conveniente (costo < costo oportunidad)
        // Si VAN < 0: El préstamo es caro (costo > costo oportunidad)
        
        // Excel: VAN = Prestamo + VNA(COKi, -Flujo)
        // Donde Flujo son valores positivos (pagos), entonces -Flujo son negativos
        
        double van = amountToFinance.doubleValue(); // Flujo inicial positivo

        // Restar valor presente de cada pago
        for (int t = 1; t <= schedule.size(); t++) {
            PaymentSchedule payment = schedule.get(t - 1);
            double cashFlow = payment.getTotalPayment().doubleValue(); // Pago positivo

            double discountFactor = Math.pow(1 + monthlyDiscountRate, t);
            double presentValue = cashFlow / discountFactor;

            van -= presentValue; // Restar porque son salidas de dinero
        }

        log.debug("VAN calculado: {}", van);
        return BigDecimal.valueOf(van).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateTIR(
            BigDecimal amountToFinance,
            List<PaymentSchedule> schedule) {

        log.debug("Calculando TIR mediante método de Newton-Raphson");

        // Construir array de flujos
        double[] cashFlows = new double[schedule.size() + 1];
        cashFlows[0] = amountToFinance.doubleValue(); // Flujo inicial positivo
        
        for (int i = 0; i < schedule.size(); i++) {
            cashFlows[i + 1] = -schedule.get(i).getTotalPayment().doubleValue(); // Pagos negativos
        }

        // Calcular TIR usando Newton-Raphson (más preciso que bisección)
        double monthlyTIR = calculateIRRNewtonRaphson(cashFlows);

        // Convertir TIR mensual a anual: TCEA = (1 + TIR_mensual)^12 - 1
        double annualTIR = Math.pow(1 + monthlyTIR, 12) - 1;

        BigDecimal tirPercentage = BigDecimal.valueOf(annualTIR * 100)
                .setScale(RATE_SCALE, RoundingMode.HALF_UP);

        log.debug("TIR mensual: {}%, TIR anual: {}%", monthlyTIR * 100, tirPercentage);
        return tirPercentage;
    }

    @Override
    public BigDecimal calculateTCEA(
            BigDecimal amountToFinance,
            List<PaymentSchedule> schedule,
            BigDecimal additionalCosts) {

        log.debug("Calculando TCEA incluyendo todos los costos");

        // TCEA considera el flujo neto recibido (préstamo - costos iniciales)
        // Pero los costos ya están capitalizados en amountToFinance, entonces:
        // Flujo_0 = amountToFinance (ya incluye costos capitalizados)
        
        // Construir array de flujos igual que TIR
        // Excel: TCEA = (1 + TIR)^NCxA - 1, donde NCxA = 12 para mensual
        double[] cashFlows = new double[schedule.size() + 1];
        cashFlows[0] = amountToFinance.doubleValue();
        
        for (int i = 0; i < schedule.size(); i++) {
            cashFlows[i + 1] = -schedule.get(i).getTotalPayment().doubleValue();
        }

        // Calcular TIR mensual
        double monthlyTIR = calculateIRRNewtonRaphson(cashFlows);

        // TCEA = (1 + TIR_mensual)^12 - 1 (igual que Excel)
        double annualTCEA = Math.pow(1 + monthlyTIR, 12) - 1;

        BigDecimal tceaPercentage = BigDecimal.valueOf(annualTCEA * 100)
                .setScale(RATE_SCALE, RoundingMode.HALF_UP);

        log.debug("TCEA calculada: {}%", tceaPercentage);
        return tceaPercentage;
    }

    // ============ Métodos Privados ============

    /**
     * Calcula la TIR usando el método de Newton-Raphson.
     * Más preciso y rápido que bisección para funciones suaves.
     */
    private double calculateIRRNewtonRaphson(double[] cashFlows) {
        double guess = 0.01; // Estimación inicial: 1% mensual
        double tolerance = 1e-10; // Alta precisión
        int maxIterations = 1000;
        
        for (int i = 0; i < maxIterations; i++) {
            double npv = 0;
            double dnpv = 0; // Derivada del NPV
            
            for (int t = 0; t < cashFlows.length; t++) {
                double factor = Math.pow(1 + guess, t);
                npv += cashFlows[t] / factor;
                if (t > 0) {
                    dnpv -= t * cashFlows[t] / Math.pow(1 + guess, t + 1);
                }
            }
            
            if (Math.abs(dnpv) < 1e-15) {
                // Evitar división por cero
                break;
            }
            
            double newGuess = guess - npv / dnpv;
            
            if (Math.abs(newGuess - guess) < tolerance) {
                return newGuess;
            }
            
            guess = newGuess;
        }
        
        return guess;
    }

    /**
     * Calcula VAN con una tasa específica (para uso interno)
     */
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
}
