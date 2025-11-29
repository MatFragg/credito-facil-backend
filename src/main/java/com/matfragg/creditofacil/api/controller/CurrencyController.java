package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller para operaciones de conversión de moneda
 * Soporta conversiones entre PEN (Soles) y USD (Dólares)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "API de conversión de monedas PEN/USD")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/exchange-rate")
    @Operation(
            summary = "Obtener tipo de cambio",
            description = "Obtiene el tipo de cambio actual entre dos monedas (PEN/USD)"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExchangeRate(
            @Parameter(description = "Moneda origen", example = "USD")
            @RequestParam(defaultValue = "USD") String from,
            @Parameter(description = "Moneda destino", example = "PEN")
            @RequestParam(defaultValue = "PEN") String to) {
        
        log.debug("Consultando tipo de cambio {} -> {}", from, to);
        
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        BigDecimal inverseRate = currencyService.getExchangeRate(to, from);
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", from.toUpperCase());
        data.put("to", to.toUpperCase());
        data.put("rate", rate);
        data.put("fromSymbol", currencyService.getCurrencySymbol(from));
        data.put("toSymbol", currencyService.getCurrencySymbol(to));
        data.put("inverseRate", inverseRate);
        data.put("description", String.format("1 %s = %s %s", 
                from.toUpperCase(), rate, to.toUpperCase()));
        
        return ResponseEntity.ok(ApiResponse.success("Tipo de cambio obtenido exitosamente", data));
    }

    @GetMapping("/convert")
    @Operation(
            summary = "Convertir monto",
            description = "Convierte un monto de una moneda a otra"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> convert(
            @Parameter(description = "Monto a convertir", example = "50000", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "Moneda origen", example = "USD")
            @RequestParam(defaultValue = "USD") String from,
            @Parameter(description = "Moneda destino", example = "PEN")
            @RequestParam(defaultValue = "PEN") String to) {
        
        log.debug("Convirtiendo {} {} a {}", amount, from, to);
        
        BigDecimal converted = currencyService.convert(amount, from, to);
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("originalAmount", amount);
        data.put("originalCurrency", from.toUpperCase());
        data.put("originalSymbol", currencyService.getCurrencySymbol(from));
        data.put("convertedAmount", converted);
        data.put("targetCurrency", to.toUpperCase());
        data.put("targetSymbol", currencyService.getCurrencySymbol(to));
        data.put("exchangeRate", rate);
        data.put("formatted", String.format("%s %s = %s %s", 
                currencyService.getCurrencySymbol(from), amount,
                currencyService.getCurrencySymbol(to), converted));
        
        return ResponseEntity.ok(ApiResponse.success("Conversión realizada exitosamente", data));
    }

    @GetMapping("/supported")
    @Operation(
            summary = "Listar monedas soportadas",
            description = "Obtiene la lista de monedas soportadas por el sistema"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSupportedCurrencies() {
        
        Map<String, Object> currencies = new LinkedHashMap<>();
        
        Map<String, Object> pen = new LinkedHashMap<>();
        pen.put("code", "PEN");
        pen.put("name", "Sol Peruano");
        pen.put("symbol", "S/");
        pen.put("country", "Perú");
        
        Map<String, Object> usd = new LinkedHashMap<>();
        usd.put("code", "USD");
        usd.put("name", "Dólar Estadounidense");
        usd.put("symbol", "$");
        usd.put("country", "Estados Unidos");
        
        currencies.put("PEN", pen);
        currencies.put("USD", usd);
        currencies.put("default", "PEN");
        
        return ResponseEntity.ok(ApiResponse.success("Monedas soportadas", currencies));
    }
}
