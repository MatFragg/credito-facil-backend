package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.SimulationRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.PaymentScheduleResponse;
import com.matfragg.creditofacil.api.dto.response.SimulationResponse;
import com.matfragg.creditofacil.api.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulations", description = "API de simulación de créditos hipotecarios")
@SecurityRequirement(name = "bearerAuth")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/calculate")
    @Operation(summary = "Calcular simulación", description = "Calcula una simulación sin guardarla (preview)")
    public ResponseEntity<ApiResponse<SimulationResponse>> calculate(@Valid @RequestBody SimulationRequest request) {
        SimulationResponse calculated = simulationService.calculate(request);
        return ResponseEntity.ok(
                ApiResponse.<SimulationResponse>builder()
                        .success(true)
                        .message("Simulación calculada exitosamente")
                        .data(calculated)
                        .build()
        );
    }

    @PostMapping
    @Operation(summary = "Guardar simulación", description = "Calcula y guarda una simulación completa con su cronograma")
    public ResponseEntity<ApiResponse<SimulationResponse>> save(@Valid @RequestBody SimulationRequest request) {
        SimulationResponse saved = simulationService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SimulationResponse>builder()
                        .success(true)
                        .message("Simulación guardada exitosamente")
                        .data(saved)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las simulaciones", description = "Obtiene todas las simulaciones con paginación (ADMIN)")
    public ResponseEntity<ApiResponse<Page<SimulationResponse>>> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SimulationResponse> simulations = simulationService.findAll(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<SimulationResponse>>builder()
                        .success(true)
                        .message("Simulaciones obtenidas exitosamente")
                        .data(simulations)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener simulación por ID", description = "Obtiene una simulación específica por su ID")
    public ResponseEntity<ApiResponse<SimulationResponse>> findById(@PathVariable Long id) {
        SimulationResponse simulation = simulationService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<SimulationResponse>builder()
                        .success(true)
                        .message("Simulación encontrada")
                        .data(simulation)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar simulación", description = "Actualiza una simulación y recalcula su cronograma")
    public ResponseEntity<ApiResponse<SimulationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SimulationRequest request) {
        SimulationResponse updated = simulationService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.<SimulationResponse>builder()
                        .success(true)
                        .message("Simulación actualizada exitosamente")
                        .data(updated)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar simulación", description = "Elimina una simulación y su cronograma (ADMIN)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        simulationService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Simulación eliminada exitosamente")
                        .build()
        );
    }

    @GetMapping("/my-simulations")
    @Operation(summary = "Obtener mis simulaciones", description = "Obtiene las simulaciones del cliente autenticado con paginación")
    public ResponseEntity<ApiResponse<Page<SimulationResponse>>> getMySimulations(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SimulationResponse> mySimulations = simulationService.getMySimulations(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<SimulationResponse>>builder()
                        .success(true)
                        .message("Mis simulaciones obtenidas exitosamente")
                        .data(mySimulations)
                        .build()
        );
    }

    @GetMapping("/{id}/schedule")
    @Operation(summary = "Obtener cronograma de pagos", description = "Obtiene el cronograma completo de pagos de una simulación")
    public ResponseEntity<ApiResponse<List<PaymentScheduleResponse>>> getSchedule(@PathVariable Long id) {
        List<PaymentScheduleResponse> schedule = simulationService.getSchedule(id);
        return ResponseEntity.ok(
                ApiResponse.<List<PaymentScheduleResponse>>builder()
                        .success(true)
                        .message("Cronograma de pagos obtenido exitosamente")
                        .data(schedule)
                        .build()
        );
    }
}
