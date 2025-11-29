package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/**
 * Implementación del servicio de conversión de monedas
 * Soporta PEN (Soles) y USD (Dólares)
 */
@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("PEN", "USD");
    private static final int SCALE = 4;
    private static final int AMOUNT_SCALE = 2;
    
    private static final Map<String, String> CURRENCY_SYMBOLS = Map.of(
            "PEN", "S/",
            "USD", "$"
    );

    /**
     * Tipo de cambio USD -> PEN configurable desde application.properties
     * Valor por defecto: 3.75 (aproximado del mercado peruano)
     */
    @Value("${app.exchange-rate.usd-to-pen:3.75}")
    private BigDecimal usdToPenRate;

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Normalizar a mayúsculas
        fromCurrency = normalizeAndValidate(fromCurrency);
        toCurrency = normalizeAndValidate(toCurrency);

        // Si son la misma moneda, no hay conversión
        if (fromCurrency.equals(toCurrency)) {
            return amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        BigDecimal converted = amount.multiply(rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);

        log.debug("Conversión: {} {} -> {} {} (TC: {})", 
                amount, fromCurrency, converted, toCurrency, rate);

        return converted;
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        fromCurrency = normalizeAndValidate(fromCurrency);
        toCurrency = normalizeAndValidate(toCurrency);

        // Misma moneda
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        // USD -> PEN
        if ("USD".equals(fromCurrency) && "PEN".equals(toCurrency)) {
            log.debug("Tipo de cambio USD->PEN: {}", usdToPenRate);
            return usdToPenRate;
        }

        // PEN -> USD (inverso)
        if ("PEN".equals(fromCurrency) && "USD".equals(toCurrency)) {
            BigDecimal inverseRate = BigDecimal.ONE.divide(usdToPenRate, SCALE, RoundingMode.HALF_UP);
            log.debug("Tipo de cambio PEN->USD: {}", inverseRate);
            return inverseRate;
        }

        throw new BadRequestException("Conversión no soportada: " + fromCurrency + " -> " + toCurrency);
    }

    @Override
    public boolean isSupported(String currency) {
        if (currency == null || currency.isBlank()) {
            return false;
        }
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase().trim());
    }

    @Override
    public String getCurrencySymbol(String currency) {
        if (!isSupported(currency)) {
            return "";
        }
        return CURRENCY_SYMBOLS.getOrDefault(currency.toUpperCase().trim(), "");
    }

    @Override
    public String getAlternateCurrency(String currency) {
        if (!isSupported(currency)) {
            return "PEN"; // Default
        }
        return "PEN".equalsIgnoreCase(currency) ? "USD" : "PEN";
    }

    /**
     * Normaliza y valida el código de moneda
     */
    private String normalizeAndValidate(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new BadRequestException("El código de moneda es requerido");
        }
        
        String normalized = currency.toUpperCase().trim();
        
        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw new BadRequestException(
                    "Moneda no soportada: " + currency + ". Monedas válidas: PEN, USD");
        }
        
        return normalized;
    }
}
