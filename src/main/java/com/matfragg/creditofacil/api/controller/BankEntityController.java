package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.BankEntityRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.BankEntityResponse;
import com.matfragg.creditofacil.api.dto.response.DownPaymentInfoResponse;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.model.entities.BankEntity;
import com.matfragg.creditofacil.api.repository.BankEntityRepository;
import com.matfragg.creditofacil.api.service.BankEntityService;
import com.matfragg.creditofacil.api.service.DownPaymentValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-entities")
@RequiredArgsConstructor
@Tag(name = "Bank Entities", description = "API de gestión de entidades bancarias")
@SecurityRequirement(name = "bearerAuth")
public class BankEntityController {

    private final BankEntityService bankEntityService;
    private final DownPaymentValidationService downPaymentValidationService;
    private final BankEntityRepository bankEntityRepository;

    @GetMapping
    @Operation(summary = "Listar todas las entidades bancarias", description = "Obtiene todas las entidades bancarias disponibles. Usar query param 'name' para buscar por nombre.")
    public ResponseEntity<ApiResponse<List<BankEntityResponse>>> findAll(
            @RequestParam(required = false) String name) {
        List<BankEntityResponse> banks;
        if (name != null && !name.trim().isEmpty()) {
            BankEntityResponse bank = bankEntityService.findByName(name);
            banks = List.of(bank);
        } else {
            banks = bankEntityService.findAll();
        }
        return ResponseEntity.ok(
                ApiResponse.<List<BankEntityResponse>>builder()
                        .success(true)
                        .message(name != null ? "Entidad bancaria encontrada" : "Entidades bancarias obtenidas exitosamente")
                        .data(banks)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener entidad bancaria por ID", description = "Obtiene una entidad bancaria específica por su ID")
    public ResponseEntity<ApiResponse<BankEntityResponse>> findById(@PathVariable Long id) {
        BankEntityResponse bank = bankEntityService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<BankEntityResponse>builder()
                        .success(true)
                        .message("Entidad bancaria encontrada")
                        .data(bank)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar entidad bancaria", description = "Actualiza tasas y condiciones de una entidad bancaria (ADMIN)")
    public ResponseEntity<ApiResponse<BankEntityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BankEntityRequest request) {
        BankEntityResponse updated = bankEntityService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.<BankEntityResponse>builder()
                        .success(true)
                        .message("Entidad bancaria actualizada exitosamente")
                        .data(updated)
                        .build()
        );
    }

    @GetMapping("/comparisons")
    @Operation(summary = "Comparar entidades bancarias", description = "Compara múltiples entidades bancarias por sus IDs")
    public ResponseEntity<ApiResponse<List<BankEntityResponse>>> getComparisons(
            @RequestParam(name = "ids") List<Long> bankIds) {

        if (bankIds == null || bankIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Provide at least one bank id"));
        }
        List<BankEntityResponse> banks = bankEntityService.compare(bankIds);
        return ResponseEntity.ok(ApiResponse.success("Comparación realizada exitosamente", banks));
    }

    @GetMapping("/down-payment-info")
    @Operation(summary = "Consultar requisitos de cuota inicial", 
               description = "Obtiene los requisitos de cuota inicial mínima y financiamiento máximo según el precio de la vivienda y entidad bancaria")
    public ResponseEntity<ApiResponse<DownPaymentInfoResponse>> getDownPaymentInfo(
            @RequestParam BigDecimal propertyPrice,
            @RequestParam Long bankEntityId) {
        
        BankEntity bankEntity = bankEntityRepository.findById(bankEntityId)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad bancaria no encontrada"));
        
        BigDecimal minDownPaymentPct = downPaymentValidationService.calculateMinimumDownPaymentPercentage(
                propertyPrice, bankEntity);
        BigDecimal minDownPaymentAmt = downPaymentValidationService.calculateMinimumDownPaymentAmount(
                propertyPrice, bankEntity);
        BigDecimal maxFinancingPct = downPaymentValidationService.calculateMaxFinancingPercentage(
                propertyPrice, bankEntity);
        BigDecimal maxFinancingAmt = downPaymentValidationService.calculateMaxFinancingAmount(
                propertyPrice, bankEntity);
        
        String priceRange = propertyPrice.compareTo(bankEntity.getPriceThreshold()) <= 0 ? "LOW" : "HIGH";
        
        DownPaymentInfoResponse response = DownPaymentInfoResponse.builder()
                .propertyPrice(propertyPrice)
                .minimumDownPaymentPercentage(minDownPaymentPct)
                .minimumDownPaymentAmount(minDownPaymentAmt)
                .maximumFinancingPercentage(maxFinancingPct)
                .maximumFinancingAmount(maxFinancingAmt)
                .priceRange(priceRange)
                .bankEntityName(bankEntity.getName())
                .build();
        
        return ResponseEntity.ok(
                ApiResponse.<DownPaymentInfoResponse>builder()
                        .success(true)
                        .message("Información de cuota inicial obtenida exitosamente")
                        .data(response)
                        .build()
        );
    }
}
