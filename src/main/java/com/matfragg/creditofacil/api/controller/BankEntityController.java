package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.BankEntityRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.BankEntityResponse;
import com.matfragg.creditofacil.api.service.BankEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-entities")
@RequiredArgsConstructor
@Tag(name = "Bank Entities", description = "API de gestión de entidades bancarias")
@SecurityRequirement(name = "bearerAuth")
public class BankEntityController {

    private final BankEntityService bankEntityService;

    @GetMapping
    @Operation(summary = "Listar todas las entidades bancarias", description = "Obtiene todas las entidades bancarias disponibles")
    public ResponseEntity<ApiResponse<List<BankEntityResponse>>> findAll() {
        List<BankEntityResponse> banks = bankEntityService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<BankEntityResponse>>builder()
                        .success(true)
                        .message("Entidades bancarias obtenidas exitosamente")
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

    @GetMapping("/search")
    @Operation(summary = "Buscar entidad bancaria por nombre", description = "Busca una entidad bancaria por su nombre")
    public ResponseEntity<ApiResponse<BankEntityResponse>> findByName(@RequestParam String name) {
        BankEntityResponse bank = bankEntityService.findByName(name);
        return ResponseEntity.ok(
                ApiResponse.<BankEntityResponse>builder()
                        .success(true)
                        .message("Entidad bancaria encontrada")
                        .data(bank)
                        .build()
        );
    }

    @PostMapping("/compare")
    @Operation(summary = "Comparar entidades bancarias", description = "Compara múltiples entidades bancarias")
    public ResponseEntity<ApiResponse<List<BankEntityResponse>>> compare(@RequestBody List<Long> bankIds) {
        List<BankEntityResponse> banks = bankEntityService.compare(bankIds);
        return ResponseEntity.ok(
                ApiResponse.<List<BankEntityResponse>>builder()
                        .success(true)
                        .message("Comparación realizada exitosamente")
                        .data(banks)
                        .build()
        );
    }
}
