package com.matfragg.creditofacil.api.controller;

import com.matfragg.creditofacil.api.dto.request.ForgotPasswordRequest;
import com.matfragg.creditofacil.api.dto.request.LoginRequest;
import com.matfragg.creditofacil.api.dto.request.RefreshTokenRequest;
import com.matfragg.creditofacil.api.dto.request.RegisterRequest;
import com.matfragg.creditofacil.api.dto.request.ResetPasswordRequest;
import com.matfragg.creditofacil.api.dto.response.ApiResponse;
import com.matfragg.creditofacil.api.dto.response.AuthResponse;
import com.matfragg.creditofacil.api.dto.response.UserResponse;
import com.matfragg.creditofacil.api.service.AuthService;
import com.matfragg.creditofacil.api.service.TurnstileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación
 * Maneja el registro, login y verificación de usuarios
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para gestión de autenticación y autorización")
public class AuthController {

    private final AuthService authService;
    private final TurnstileService turnstileService;

    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param request Datos del usuario a registrar
     * @return ApiResponse con los datos del usuario creado
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario. La cuenta estará inactiva hasta verificar el email.")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        // Validar Turnstile antes de procesar el registro
        turnstileService.validateToken(request.getTurnstileToken(), getClientIp(httpRequest));
        
        UserResponse userResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente. Por favor verifica tu correo electrónico.", userResponse));
    }

    /**
     * Autentica un usuario y genera un token JWT
     * 
     * @param request Credenciales del usuario (email y password)
     * @return ApiResponse con el token JWT y datos del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        // Validar Turnstile antes de procesar el login
        turnstileService.validateToken(request.getTurnstileToken(), getClientIp(httpRequest));
        
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", authResponse));
    }

    /**
     * Verifica la cuenta de un usuario mediante un token
     * 
     * @param token Token de verificación enviado por email
     * @return ApiResponse con los datos del usuario verificado
     */
    @PostMapping("/verify")
    @Operation(summary = "Verificar cuenta", description = "Verifica la cuenta de un usuario usando el token enviado por email")
    public ResponseEntity<ApiResponse<UserResponse>> verifyAccount(@RequestParam String token) {
        UserResponse userResponse = authService.verifyAccount(token);
        return ResponseEntity.ok(ApiResponse.success("Cuenta verificada exitosamente", userResponse));
    }

    /**
     * Obtiene los datos del usuario actual autenticado
     * 
     * @return ApiResponse con los datos del usuario
     */
    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", description = "Obtiene los datos del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    /**
     * Refresca el token JWT usando un refresh token válido
     * 
     * @param request Contiene el refresh token
     * @return ApiResponse con el nuevo token JWT
     */
    @PutMapping("/tokens")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo token JWT usando un refresh token válido")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refrescado exitosamente", authResponse));
    }

    /**
     * Envía un email con instrucciones para restablecer la contraseña
     * 
     * @param request Contiene el email del usuario
     * @return ApiResponse confirmando el envío del email
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Olvidé mi contraseña", description = "Envía un email con instrucciones para restablecer la contraseña")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Si el correo existe, recibirás instrucciones para restablecer tu contraseña", null));
    }

    /**
     * Restablece la contraseña usando un token válido
     * 
     * @param request Contiene el token y la nueva contraseña
     * @return ApiResponse confirmando el restablecimiento
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando un token válido")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña restablecida exitosamente", null));
    }

    /**
     * Obtiene la IP del cliente considerando proxies y load balancers
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs, tomar la primera
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
