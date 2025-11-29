package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.exception.BadRequestException;
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
     * Valida que el valor de la vivienda esté dentro del rango NCMV
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria con los rangos configurados
     * @param useCRC Si se usa Cobertura de Riesgo Crediticio (permite mayor valor)
     * @throws BadRequestException si el precio está fuera del rango permitido
     */
    public void validateNCMVPropertyRange(
            BigDecimal propertyPrice, 
            BankEntity bankEntity,
            boolean useCRC) {
        
        BigDecimal minValue = bankEntity.getNcmvMinPropertyValue();
        BigDecimal maxValue = useCRC 
                ? bankEntity.getNcmvMaxPropertyValueCRC() 
                : bankEntity.getNcmvMaxPropertyValue();
        
        if (propertyPrice.compareTo(minValue) < 0) {
            String message = String.format(
                "El valor de la vivienda (S/ %.2f) es menor al mínimo permitido por el NCMV (S/ %.2f)",
                propertyPrice.doubleValue(), minValue.doubleValue()
            );
            log.warn(message);
            throw new BadRequestException(message);
        }
        
        if (propertyPrice.compareTo(maxValue) > 0) {
            String message = String.format(
                "El valor de la vivienda (S/ %.2f) excede el máximo permitido por el NCMV %s(S/ %.2f). " +
                "Para viviendas de mayor valor, consulte otras opciones de financiamiento.",
                propertyPrice.doubleValue(),
                useCRC ? "con CRC " : "",
                maxValue.doubleValue()
            );
            log.warn(message);
            throw new BadRequestException(message);
        }
        
        log.debug("Validación NCMV exitosa - Precio: S/ {} dentro del rango S/ {} - S/ {}",
                propertyPrice, minValue, maxValue);
    }

	/**
     * Calcula el monto del Premio al Buen Pagador (PBP) según el valor de la vivienda
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria con los montos de PBP configurados
     * @return Monto del PBP (S/ 6,400, S/ 17,700, o S/ 0 si no califica)
     */
    public BigDecimal calculatePBPAmount(BigDecimal propertyPrice, BankEntity bankEntity) {
        BigDecimal ncmvMin = bankEntity.getNcmvMinPropertyValue();
        BigDecimal pbpThreshold = bankEntity.getPbpThresholdLow();
        BigDecimal ncmvMax = bankEntity.getNcmvMaxPropertyValue();
        
        // PBP Standard: S/ 68,800 - S/ 102,900 → S/ 6,400
        if (propertyPrice.compareTo(ncmvMin) >= 0 && propertyPrice.compareTo(pbpThreshold) < 0) {
            log.debug("PBP Standard aplicable - Vivienda en rango S/ {} - S/ {}", ncmvMin, pbpThreshold);
            return bankEntity.getPbpAmountStandard();
        }
        
        // PBP Plus: S/ 102,900 - S/ 362,100 → S/ 17,700
        if (propertyPrice.compareTo(pbpThreshold) >= 0 && propertyPrice.compareTo(ncmvMax) <= 0) {
            log.debug("PBP Plus aplicable - Vivienda en rango S/ {} - S/ {}", pbpThreshold, ncmvMax);
            return bankEntity.getPbpAmountPlus();
        }
        
        log.debug("PBP no aplicable para vivienda de S/ {}", propertyPrice);
        return BigDecimal.ZERO;
    }

	/**
     * Verifica si la vivienda califica para el Premio al Buen Pagador
     * 
     * @param propertyPrice Precio de la vivienda
     * @param bankEntity Entidad bancaria
     * @return true si califica para PBP
     */
    public boolean qualifiesForPBP(BigDecimal propertyPrice, BankEntity bankEntity) {
        return calculatePBPAmount(propertyPrice, bankEntity).compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene mensaje informativo sobre el PBP
     */
    public String getPBPInfoMessage(BigDecimal propertyPrice, BankEntity bankEntity) {
        BigDecimal pbpAmount = calculatePBPAmount(propertyPrice, bankEntity);
        
        if (pbpAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "Esta vivienda no califica para el Premio al Buen Pagador (PBP).";
        }
        
        String type = pbpAmount.compareTo(bankEntity.getPbpAmountStandard()) == 0 
                ? "Standard" : "Plus";
        
        return String.format(
            "Esta vivienda califica para el Premio al Buen Pagador %s de S/ %.2f. " +
            "Este descuento se aplica al saldo de deuda como reconocimiento al cumplimiento de pagos.",
            type, pbpAmount.doubleValue()
        );
    }

	/**
     * Valida la cuota inicial mínima según normativa NCMV (7.5%)
     */
    public void validateMinimumDownPaymentNCMV(
            BigDecimal propertyPrice,
            BigDecimal downPayment) {
        
        // NCMV exige mínimo 7.5% de cuota inicial
        BigDecimal minPercentage = BigDecimal.valueOf(7.5);
        BigDecimal minAmount = propertyPrice.multiply(minPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        if (downPayment.compareTo(minAmount) < 0) {
            String message = String.format(
                "La cuota inicial (S/ %.2f) es menor al mínimo requerido por el NCMV (7.5%% = S/ %.2f). " +
                "La cuota inicial mínima debe ser al menos el 7.5%% del valor de la vivienda.",
                downPayment.doubleValue(), minAmount.doubleValue()
            );
            throw new BadRequestException(message);
        }
        
        log.debug("Validación cuota inicial NCMV exitosa - Mínimo 7.5% = S/ {}, Proporcionado: S/ {}",
                minAmount, downPayment);
    }

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
