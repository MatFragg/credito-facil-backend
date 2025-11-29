package com.matfragg.creditofacil.api.service;

import java.math.BigDecimal;

/**
 * Servicio para conversión de monedas (PEN/USD)
 * Utilizado para normalizar montos en simulaciones hipotecarias
 */
public interface CurrencyService {
    
    /**
     * Convierte un monto de una moneda a otra
     * 
     * @param amount Monto a convertir
     * @param fromCurrency Moneda origen (PEN o USD)
     * @param toCurrency Moneda destino (PEN o USD)
     * @return Monto convertido
     */
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
    
    /**
     * Obtiene el tipo de cambio actual entre dos monedas
     * 
     * @param fromCurrency Moneda origen
     * @param toCurrency Moneda destino
     * @return Tipo de cambio
     */
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
    
    /**
     * Valida si una moneda es soportada por el sistema
     * 
     * @param currency Código de moneda
     * @return true si la moneda es soportada
     */
    boolean isSupported(String currency);
    
    /**
     * Obtiene el símbolo de la moneda
     * 
     * @param currency Código de moneda (PEN o USD)
     * @return Símbolo (S/ o $)
     */
    String getCurrencySymbol(String currency);
    
    /**
     * Obtiene la moneda alternativa
     * 
     * @param currency Moneda actual
     * @return Moneda alternativa (si PEN devuelve USD y viceversa)
     */
    String getAlternateCurrency(String currency);
}
