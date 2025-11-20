package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.ChangePasswordRequest;
import com.matfragg.creditofacil.api.dto.request.UserProfileRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.UserResponse;
import com.matfragg.creditofacil.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de gestión de usuarios
 * Maneja operaciones CRUD y administración de perfiles de usuario
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Obtiene el perfil del usuario actual autenticado
     * 
     * @return ApiResponse con los datos del usuario
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener perfil actual", description = "Obtiene el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse userResponse = userService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Actualiza el perfil del usuario actual autenticado
     * 
     * @param request Datos del perfil a actualizar
     * @return ApiResponse con los datos actualizados
     */
    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil", description = "Actualiza el perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UserProfileRequest request) {
        UserResponse userResponse = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado exitosamente", userResponse));
    }

    /**
     * Cambia la contraseña del usuario actual autenticado
     * 
     * @param request Contiene la contraseña actual y la nueva
     * @return ApiResponse confirmando el cambio
     */
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña cambiada exitosamente", null));
    }

    /**
     * Busca un usuario por su ID (solo ADMIN)
     * 
     * @param id ID del usuario a buscar
     * @return ApiResponse con los datos del usuario
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuario por ID", description = "Obtiene un usuario específico por su ID (solo administradores)")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {
        UserResponse userResponse = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario a buscar
     * @return ApiResponse con los datos del usuario
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios con paginación. Usar query param 'email' para buscar por email (solo administradores)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String email) {
        if (email != null && !email.trim().isEmpty()) {
            UserResponse user = userService.findByEmail(email);
            Page<UserResponse> singleUserPage = new PageImpl<>(List.of(user), pageable, 1);
            return ResponseEntity.ok(ApiResponse.success(singleUserPage));
        }
        Page<UserResponse> users = userService.listUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Elimina un usuario por su ID (solo ADMIN)
     * 
     * @param id ID del usuario a eliminar
     * @return ApiResponse confirmando la eliminación
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID (solo administradores)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado exitosamente", null));
    }

    /**
     * Activa o desactiva un usuario (solo ADMIN)
     * 
     * @param id ID del usuario
     * @param isActive Estado a establecer
     * @return ApiResponse con los datos actualizados
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/Desactivar usuario", description = "Cambia el estado activo/inactivo de un usuario (solo administradores)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        UserResponse userResponse = userService.toggleUserStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(
                isActive ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente",
                userResponse));
    }
}
