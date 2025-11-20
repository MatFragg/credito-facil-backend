package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.SettingsRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.SettingsResponse;
import com.matfragg.creditofacil.api.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "API de configuración de simulaciones")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las configuraciones", description = "Obtiene todas las configuraciones con paginación (ADMIN)")
    public ResponseEntity<ApiResponse<Page<SettingsResponse>>> listAll(@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SettingsResponse> settings = settingsService.findAll(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<SettingsResponse>>builder()
                        .success(true)
                        .message("Configuraciones obtenidas exitosamente")
                        .data(settings)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener configuración por ID", description = "Obtiene una configuración específica por su ID")
    public ResponseEntity<ApiResponse<SettingsResponse>> findById(@PathVariable Long id) {
        SettingsResponse settings = settingsService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<SettingsResponse>builder()
                        .success(true)
                        .message("Configuración encontrada")
                        .data(settings)
                        .build()
        );
    }

    @PostMapping
    @Operation(summary = "Crear configuración", description = "Crea una nueva configuración de simulación")
    public ResponseEntity<ApiResponse<SettingsResponse>> create(@Valid @RequestBody SettingsRequest request) {
        SettingsResponse created = settingsService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SettingsResponse>builder()
                        .success(true)
                        .message("Configuración creada exitosamente")
                        .data(created)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar configuración", description = "Actualiza una configuración existente")
    public ResponseEntity<ApiResponse<SettingsResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SettingsRequest request) {
        SettingsResponse updated = settingsService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.<SettingsResponse>builder()
                        .success(true)
                        .message("Configuración actualizada exitosamente")
                        .data(updated)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar configuración", description = "Elimina una configuración (ADMIN)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        settingsService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Configuración eliminada exitosamente")
                        .build()
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mi configuración", description = "Obtiene la configuración del usuario autenticado")
    public ResponseEntity<ApiResponse<SettingsResponse>> getMySettings() {
        SettingsResponse settings = settingsService.getMySettings();
        return ResponseEntity.ok(
                ApiResponse.<SettingsResponse>builder()
                        .success(true)
                        .message("Configuración obtenida exitosamente")
                        .data(settings)
                        .build()
        );
    }
}
