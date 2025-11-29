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

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "API de gestión de propiedades")
@SecurityRequirement(name = "bearerAuth")
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    @Operation(summary = "Listar propiedades", description = "Obtiene propiedades con paginación; opcional filter por propertyCode")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> findAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "propertyCode", required = false) String propertyCode) {

        if (propertyCode != null && !propertyCode.trim().isEmpty()) {
            PropertyResponse property = propertyService.findByPropertyCode(propertyCode);
            Page<PropertyResponse> single = new PageImpl<>(List.of(property), pageable, 1);
            ApiResponse<Page<PropertyResponse>> response = ApiResponse.<Page<PropertyResponse>>builder()
                    .success(true)
                    .message("Propiedad encontrada")
                    .data(single)
                    .build();
            return ResponseEntity.ok(response);
        }

        Page<PropertyResponse> properties = propertyService.findAll(pageable);
        ApiResponse<Page<PropertyResponse>> response = ApiResponse.<Page<PropertyResponse>>builder()
                .success(true)
                .message("Propiedades obtenidas exitosamente")
                .data(properties)
                .build();
        return ResponseEntity.ok(response);
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
                            .build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear nueva propiedad (JSON)", description = "Crea una nueva propiedad sin imagen usando JSON")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.create(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<PropertyResponse>builder()
                            .success(true)
                            .message("Propiedad creada exitosamente")
                            .data(created)
                            .build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear nueva propiedad con imagen", description = "Crea una nueva propiedad con imagen")
    public ResponseEntity<ApiResponse<PropertyResponse>> createWithImage(
                    @io.swagger.v3.oas.annotations.Parameter(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart("property") @Valid PropertyRequest request,
                    @RequestPart(value = "image", required = false) MultipartFile image) {
            PropertyResponse created = propertyService.create(request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                            ApiResponse.<PropertyResponse>builder()
                                            .success(true)
                                            .message("Propiedad creada exitosamente")
                                            .data(created)
                                            .build());
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
                                            .build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar propiedad con imagen", description = "Actualiza una propiedad existente con nueva imagen")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateWithImage(
                    @PathVariable Long id,
                    @io.swagger.v3.oas.annotations.Parameter(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestPart("property") @Valid PropertyRequest request,
                    @RequestPart(value = "image", required = false) MultipartFile image) {
            PropertyResponse updated = propertyService.update(id, request, image);
            return ResponseEntity.ok(
                            ApiResponse.<PropertyResponse>builder()
                                            .success(true)
                                            .message("Propiedad actualizada exitosamente")
                                            .data(updated)
                                            .build());
    }

    @PatchMapping("/{id}/image")
    @Operation(summary = "Actualizar solo imagen", description = "Actualiza solo la imagen de una propiedad")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateImage(
                    @PathVariable Long id,
                    @RequestParam("image") MultipartFile image) {
            PropertyResponse updated = propertyService.updateImage(id, image);
            return ResponseEntity.ok(
                            ApiResponse.<PropertyResponse>builder()
                                            .success(true)
                                            .message("Imagen actualizada exitosamente")
                                            .data(updated)
                                            .build());
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Eliminar imagen", description = "Elimina solo la imagen de una propiedad")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long id) {
            propertyService.deleteImage(id);
            return ResponseEntity.ok(
                            ApiResponse.<Void>builder()
                                            .success(true)
                                            .message("Imagen eliminada exitosamente")
                                            .build());
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
                                            .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mis propiedades", description = "Obtiene las propiedades del cliente autenticado con paginación")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getMyProperties(
                    @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
            Page<PropertyResponse> myProperties = propertyService.getMyProperties(pageable);
            return ResponseEntity.ok(
                            ApiResponse.<Page<PropertyResponse>>builder()
                                            .success(true)
                                            .message("Mis propiedades obtenidas exitosamente")
                                            .data(myProperties)
                                            .build());
    }
}
