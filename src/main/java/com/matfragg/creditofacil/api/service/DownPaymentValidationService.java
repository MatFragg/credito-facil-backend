package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.model.entities.BankEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para validar cuotas iniciales y montos de financiamiento
 * según las reglas del Fondo MiVivienda
 */
@Slf4j
@Service
public class DownPaymentValidationService {

    /**
     * Calcula el porcentaje mínimo de cuota inicial según el precio de la vivienda
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria con sus reglas específicas
     * @return Porcentaje mínimo de cuota inicial (7.5% o 10%)
     */
    public BigDecimal calculateMinimumDownPaymentPercentage(
            BigDecimal propertyPrice, 
            BankEntity bankEntity) {
        
        if (propertyPrice.compareTo(bankEntity.getPriceThreshold()) <= 0) {
            log.debug("Vivienda en rango bajo (≤ S/ {}): {}% cuota inicial", 
                    bankEntity.getPriceThreshold(), 
                    bankEntity.getMinDownPaymentLowRange());
            return bankEntity.getMinDownPaymentLowRange();
        } else {
            log.debug("Vivienda en rango alto (> S/ {}): {}% cuota inicial", 
                    bankEntity.getPriceThreshold(), 
                    bankEntity.getMinDownPaymentHighRange());
            return bankEntity.getMinDownPaymentHighRange();
        }
    }

    /**
     * Calcula el monto mínimo de cuota inicial en soles
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria
     * @return Monto mínimo de cuota inicial en S/
     */
    public BigDecimal calculateMinimumDownPaymentAmount(
            BigDecimal propertyPrice, 
            BankEntity bankEntity) {
        
        BigDecimal percentage = calculateMinimumDownPaymentPercentage(propertyPrice, bankEntity);
        BigDecimal amount = propertyPrice.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.debug("Cuota inicial mínima calculada: S/ {} ({}% de S/ {})", 
                amount, percentage, propertyPrice);
        
        return amount;
    }

    /**
     * Calcula el porcentaje máximo de financiamiento según el precio
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria
     * @return Porcentaje máximo de financiamiento (92.5% o 90%)
     */
    public BigDecimal calculateMaxFinancingPercentage(
            BigDecimal propertyPrice, 
            BankEntity bankEntity) {
        
        if (propertyPrice.compareTo(bankEntity.getPriceThreshold()) <= 0) {
            log.debug("Financiamiento máximo: {}%", bankEntity.getMaxFinancingLowRange());
            return bankEntity.getMaxFinancingLowRange();
        } else {
            log.debug("Financiamiento máximo: {}%", bankEntity.getMaxFinancingHighRange());
            return bankEntity.getMaxFinancingHighRange();
        }
    }

    /**
     * Calcula el monto máximo de financiamiento en soles
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria
     * @return Monto máximo que se puede financiar
     */
    public BigDecimal calculateMaxFinancingAmount(
            BigDecimal propertyPrice,
            BankEntity bankEntity) {
        
        BigDecimal maxPercentage = calculateMaxFinancingPercentage(propertyPrice, bankEntity);
        BigDecimal maxAmount = propertyPrice.multiply(maxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.debug("Monto máximo a financiar: S/ {} ({}% de S/ {})", 
                maxAmount, maxPercentage, propertyPrice);
        
        return maxAmount;
    }

    /**
     * Valida si la cuota inicial cumple con el mínimo requerido
     * 
     * @param propertyPrice Precio de la vivienda
     * @param downPayment Cuota inicial proporcionada
     * @param bankEntity Entidad bancaria
     * @return true si la cuota inicial es válida
     */
    public boolean isDownPaymentValid(
            BigDecimal propertyPrice,
            BigDecimal downPayment,
            BankEntity bankEntity) {
        
        BigDecimal minRequired = calculateMinimumDownPaymentAmount(propertyPrice, bankEntity);
        boolean isValid = downPayment.compareTo(minRequired) >= 0;
        
        log.debug("Validación cuota inicial - Precio: S/ {}, Cuota: S/ {}, Mínimo: S/ {}, Válido: {}", 
                propertyPrice, downPayment, minRequired, isValid);
        
        return isValid;
    }

    /**
     * Valida si el monto a financiar está dentro del límite permitido
     * 
     * @param propertyPrice Precio de la vivienda
     * @param amountToFinance Monto que se desea financiar
     * @param bankEntity Entidad bancaria
     * @return true si el monto a financiar es válido
     */
    public boolean isFinancingAmountValid(
            BigDecimal propertyPrice,
            BigDecimal amountToFinance,
            BankEntity bankEntity) {
        
        BigDecimal maxAmount = calculateMaxFinancingAmount(propertyPrice, bankEntity);
        boolean isValid = amountToFinance.compareTo(maxAmount) <= 0;
        
        log.debug("Validación financiamiento - Precio: S/ {}, A Financiar: S/ {}, Máximo: S/ {}, Válido: {}", 
                propertyPrice, amountToFinance, maxAmount, isValid);
        
        return isValid;
    }

    /**
     * Valida la coherencia entre cuota inicial, monto a financiar y precio
     * 
     * @param propertyPrice Precio de la vivienda
     * @param downPayment Cuota inicial
     * @param amountToFinance Monto a financiar
     * @param governmentBonus Bono gubernamental (puede ser null o ZERO)
     * @return true si los montos son coherentes
     */
    public boolean validateAmountCoherence(
            BigDecimal propertyPrice,
            BigDecimal downPayment,
            BigDecimal amountToFinance,
            BigDecimal governmentBonus) {
        
        BigDecimal bonus = (governmentBonus != null) ? governmentBonus : BigDecimal.ZERO;
        BigDecimal calculatedTotal = downPayment.add(amountToFinance).add(bonus);
        
        // Permitir diferencia de hasta 0.01 por redondeos
        BigDecimal difference = calculatedTotal.subtract(propertyPrice).abs();
        boolean isCoherent = difference.compareTo(BigDecimal.valueOf(0.01)) <= 0;
        
        log.debug("Validación coherencia - Precio: S/ {}, CI: S/ {}, Financiar: S/ {}, Bono: S/ {}, Total: S/ {}, Coherente: {}",
                propertyPrice, downPayment, amountToFinance, bonus, calculatedTotal, isCoherent);
        
        return isCoherent;
    }

    /**
     * Obtiene un mensaje de error descriptivo para cuota inicial inválida
     */
    public String getDownPaymentErrorMessage(
            BigDecimal propertyPrice,
            BigDecimal downPayment,
            BankEntity bankEntity) {
        
        BigDecimal minRequired = calculateMinimumDownPaymentAmount(propertyPrice, bankEntity);
        BigDecimal minPct = calculateMinimumDownPaymentPercentage(propertyPrice, bankEntity);
        
        return String.format(
                "La cuota inicial debe ser al menos %.1f%% del precio de la vivienda (S/ %.2f). " +
                "Cuota proporcionada: S/ %.2f. Falta: S/ %.2f",
                minPct.doubleValue(), 
                minRequired.doubleValue(), 
                downPayment.doubleValue(),
                minRequired.subtract(downPayment).doubleValue()
        );
    }

    /**
     * Obtiene un mensaje de error descriptivo para monto a financiar inválido
     */
    public String getFinancingAmountErrorMessage(
            BigDecimal propertyPrice,
            BigDecimal amountToFinance,
            BankEntity bankEntity) {
        
        BigDecimal maxAmount = calculateMaxFinancingAmount(propertyPrice, bankEntity);
        BigDecimal maxPct = calculateMaxFinancingPercentage(propertyPrice, bankEntity);
        
        return String.format(
                "El monto a financiar excede el límite permitido de %.1f%% del precio de la vivienda (S/ %.2f). " +
                "Monto solicitado: S/ %.2f. Exceso: S/ %.2f",
                maxPct.doubleValue(), 
                maxAmount.doubleValue(), 
                amountToFinance.doubleValue(),
                amountToFinance.subtract(maxAmount).doubleValue()
        );
    }
}
