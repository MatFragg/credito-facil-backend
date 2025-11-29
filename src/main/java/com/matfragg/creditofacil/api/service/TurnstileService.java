package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.config.TurnstileConfig;
import com.matfragg.creditofacil.api.dto.response.TurnstileResponse;
import com.matfragg.creditofacil.api.exception.TurnstileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para validar tokens de Cloudflare Turnstile
 */
@Service
public class TurnstileService {

    private static final Logger logger = LoggerFactory.getLogger(TurnstileService.class);

    private final TurnstileConfig turnstileConfig;
    private final RestTemplate restTemplate;

    public TurnstileService(TurnstileConfig turnstileConfig, RestTemplate turnstileRestTemplate) {
        this.turnstileConfig = turnstileConfig;
        this.restTemplate = turnstileRestTemplate;
    }

    /**
     * Valida un token de Turnstile
     *
     * @param token    El token generado por el widget de Turnstile en el frontend
     * @param remoteIp La dirección IP del cliente (opcional pero recomendado)
     * @return true si la validación es exitosa
     * @throws TurnstileValidationException si la validación falla
     */
    public boolean validateToken(String token, String remoteIp) {
        // Si Turnstile está deshabilitado, permitir el paso
        if (!turnstileConfig.isEnabled()) {
            logger.debug("Turnstile está deshabilitado, omitiendo validación");
            return true;
        }

        // Validar que el token no esté vacío
        if (!StringUtils.hasText(token)) {
            logger.warn("Token de Turnstile vacío o nulo");
            throw new TurnstileValidationException("El token de verificación es requerido", "MISSING_TOKEN");
        }

        // Validar que la clave secreta esté configurada
        if (!StringUtils.hasText(turnstileConfig.getSecretKey())) {
            logger.error("La clave secreta de Turnstile no está configurada");
            throw new TurnstileValidationException("Error de configuración del servidor", "CONFIG_ERROR");
        }

        try {
            // Preparar los headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Preparar el body como form-urlencoded
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", turnstileConfig.getSecretKey());
            body.add("response", token);
            
            if (StringUtils.hasText(remoteIp)) {
                body.add("remoteip", remoteIp);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            logger.debug("Enviando solicitud de validación a Turnstile para IP: {}", remoteIp);

            // Realizar la solicitud a Cloudflare
            ResponseEntity<TurnstileResponse> responseEntity = restTemplate.exchange(
                    turnstileConfig.getVerifyUrl(),
                    HttpMethod.POST,
                    request,
                    TurnstileResponse.class
            );

            TurnstileResponse response = responseEntity.getBody();

            if (response == null) {
                logger.error("Respuesta nula de Turnstile");
                throw new TurnstileValidationException("No se pudo verificar el token", "NULL_RESPONSE");
            }

            if (response.isSuccess()) {
                logger.debug("Validación de Turnstile exitosa para hostname: {}", response.getHostname());
                return true;
            } else {
                String errorMessage = response.getErrorMessage();
                logger.warn("Validación de Turnstile fallida: {}", errorMessage);
                throw new TurnstileValidationException(errorMessage, "VALIDATION_FAILED");
            }

        } catch (RestClientException e) {
            logger.error("Error de comunicación con Turnstile: {}", e.getMessage());
            throw new TurnstileValidationException("Error al comunicarse con el servicio de verificación", e);
        }
    }

    /**
     * Valida un token de Turnstile sin IP remota
     *
     * @param token El token generado por el widget de Turnstile
     * @return true si la validación es exitosa
     * @throws TurnstileValidationException si la validación falla
     */
    public boolean validateToken(String token) {
        return validateToken(token, null);
    }

    /**
     * Verifica si Turnstile está habilitado
     *
     * @return true si Turnstile está habilitado
     */
    public boolean isEnabled() {
        return turnstileConfig.isEnabled();
    }
}
