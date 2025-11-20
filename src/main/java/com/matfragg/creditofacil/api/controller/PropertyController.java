package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.PropertyRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.PropertyResponse;
import com.matfragg.creditofacil.api.service.PropertyService;
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
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "API de gestión de propiedades")
@SecurityRequirement(name = "bearerAuth")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las propiedades", description = "Obtiene todas las propiedades con paginación (ADMIN)")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> findAll(@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PropertyResponse> properties = propertyService.findAll(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<PropertyResponse>>builder()
                        .success(true)
                        .message("Propiedades obtenidas exitosamente")
                        .data(properties)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener propiedad por ID", description = "Obtiene una propiedad específica por su ID")
    public ResponseEntity<ApiResponse<PropertyResponse>> findById(@PathVariable Long id) {
        PropertyResponse property = propertyService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<PropertyResponse>builder()
                        .success(true)
                        .message("Propiedad encontrada")
                        .data(property)
                        .build()
        );
    }

    @PostMapping
    @Operation(summary = "Crear nueva propiedad", description = "Crea una nueva propiedad (CATALOG o CLIENT)")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PropertyResponse>builder()
                        .success(true)
                        .message("Propiedad creada exitosamente")
                        .data(created)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar propiedad", description = "Actualiza una propiedad existente")
    public ResponseEntity<ApiResponse<PropertyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request) {
        PropertyResponse updated = propertyService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.<PropertyResponse>builder()
                        .success(true)
                        .message("Propiedad actualizada exitosamente")
                        .data(updated)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar propiedad", description = "Elimina una propiedad (ADMIN)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Propiedad eliminada exitosamente")
                        .build()
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mis propiedades", description = "Obtiene las propiedades del cliente autenticado con paginación")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getMyProperties(@ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PropertyResponse> myProperties = propertyService.getMyProperties(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<PropertyResponse>>builder()
                        .success(true)
                        .message("Mis propiedades obtenidas exitosamente")
                        .data(myProperties)
                        .build()
        );
    }
}
