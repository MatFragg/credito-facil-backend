package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.ClientRegisterRequest;
import com.matfragg.creditofacil.api.dto.request.ForgotPasswordRequest;
import com.matfragg.creditofacil.api.dto.request.LoginRequest;
import com.matfragg.creditofacil.api.dto.request.RefreshTokenRequest;
import com.matfragg.creditofacil.api.dto.request.RegisterRequest;
import com.matfragg.creditofacil.api.dto.request.ResetPasswordRequest;
import com.matfragg.creditofacil.api.dto.response.AuthResponse;
import com.matfragg.creditofacil.api.dto.response.UserResponse;

/**
 * Servicio de autenticación y autorización
 * Todos los métodos retornan DTOs para mantener la separación de capas
 */
public interface AuthService {
    
    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param request Datos del usuario a registrar
     * @return UserResponse con los datos del usuario creado
     */
    UserResponse register(RegisterRequest request);

    /**
     * Registra un nuevo cliente en el sistema
     * 
     * @param request Datos del cliente a registrar
     * @return AuthResponse con el token JWT y datos del cliente creado
     */
    UserResponse registerClient(ClientRegisterRequest request);
    
    /**
     * Autentica un usuario y genera un token JWT
     * 
     * @param request Credenciales del usuario (email y password)
     * @return AuthResponse con el token JWT y datos del usuario
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Verifica la cuenta de un usuario mediante un token
     * 
     * @param token Token de verificación enviado por email
     * @return UserResponse con los datos del usuario verificado
     */
    UserResponse verifyAccount(String token);
    
    /**
     * Obtiene los datos del usuario actual autenticado
     * 
     * @return UserResponse con los datos del usuario
     */
    UserResponse getCurrentUser();
    
    /**
     * Refresca el token JWT usando un refresh token válido
     * 
     * @param request Contiene el refresh token
     * @return AuthResponse con el nuevo token JWT
     */
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Envía un email con instrucciones para restablecer la contraseña
     * 
     * @param request Contiene el email del usuario
     */
    void forgotPassword(ForgotPasswordRequest request);
    
    /**
     * Restablece la contraseña usando un token válido
     * 
     * @param request Contiene el token y la nueva contraseña
     */
    void resetPassword(ResetPasswordRequest request);
}

