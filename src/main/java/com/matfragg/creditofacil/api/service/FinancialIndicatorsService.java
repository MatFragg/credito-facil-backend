package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para cálculo de indicadores financieros
 */
public interface FinancialIndicatorsService {

    /**
     * Calcula el VAN (Valor Actual Neto)
     * @param downPayment Inversión inicial (cuota inicial)
     * @param schedule Cronograma de pagos
     * @param discountRate Tasa de descuento anual (típicamente 10%)
     * @return VAN calculado
     */
    BigDecimal calculateVAN(
            BigDecimal downPayment,
            List<PaymentSchedule> schedule,
            BigDecimal discountRate
    );

    /**
     * Calcula la TIR (Tasa Interna de Retorno)
     * @param downPayment Inversión inicial
     * @param schedule Cronograma de pagos
     * @return TIR anualizada en porcentaje
     */
    BigDecimal calculateTIR(
            BigDecimal downPayment,
            List<PaymentSchedule> schedule
    );

    /**
     * Calcula la TCEA (Tasa de Costo Efectivo Anual)
     * Incluye todos los costos: seguros, comisiones, notariales, etc.
     * @param amountToFinance Monto financiado
     * @param schedule Cronograma de pagos (que ya incluye seguros)
     * @param additionalCosts Costos adicionales (comisión de apertura, gastos notariales, etc.)
     * @return TCEA en porcentaje
     */
    BigDecimal calculateTCEA(
            BigDecimal amountToFinance,
            List<PaymentSchedule> schedule,
            BigDecimal additionalCosts
    );
}
