package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.ClientRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.ClientResponse;
import com.matfragg.creditofacil.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de gestión de clientes
 * Maneja operaciones CRUD para clientes prospectos y registrados
 */
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clients", description = "Endpoints para gestión de clientes")
public class ClientController {

    private final ClientService clientService;

    /**
     * Lista todos los clientes con paginación
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes", description = "Lista todos los clientes con paginación (solo ADMIN). Usar query param 'dni' para buscar por DNI.")
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> listClients(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String dni) {
        if (dni != null && !dni.trim().isEmpty()) {
            ClientResponse client = clientService.findByDni(dni);
            Page<ClientResponse> singleClientPage = new PageImpl<>(List.of(client), pageable, 1);
            return ResponseEntity.ok(ApiResponse.success(singleClientPage));
        }
        Page<ClientResponse> clients = clientService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    /**
     * Busca un cliente por su ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Obtiene un cliente por su ID")
    public ResponseEntity<ApiResponse<ClientResponse>> findById(@PathVariable Long id) {
        ClientResponse clientResponse = clientService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(clientResponse));
    }

    /**
     * Crea un nuevo cliente
     */
    @PostMapping
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente (prospecto o registrado)")
    public ResponseEntity<ApiResponse<ClientResponse>> create(@Valid @RequestBody ClientRequest request) {
        ClientResponse clientResponse = clientService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cliente creado exitosamente", clientResponse));
    }

    /**
     * Actualiza los datos de un cliente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        ClientResponse clientResponse = clientService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cliente actualizado exitosamente", clientResponse));
    }

    /**
     * Elimina un cliente por su ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente por su ID (solo ADMIN)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente eliminado exitosamente", null));
    }

    /**
     * Obtiene el perfil del cliente autenticado
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener mi perfil", description = "Obtiene el perfil del cliente autenticado")
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> getMyClients(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<ClientResponse> clientResponses = clientService.getMyClients(pageable);
        return ResponseEntity.ok(ApiResponse.success(clientResponses));
    }

}
