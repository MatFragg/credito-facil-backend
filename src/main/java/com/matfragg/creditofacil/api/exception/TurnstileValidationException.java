package com.matfragg.creditofacil.api.exception;

/**
 * Excepción lanzada cuando la validación de Cloudflare Turnstile falla
 */
public class TurnstileValidationException extends RuntimeException {

    private final String errorCode;

    public TurnstileValidationException(String message) {
        super(message);
        this.errorCode = "TURNSTILE_VALIDATION_FAILED";
    }

    public TurnstileValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TurnstileValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TURNSTILE_VALIDATION_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
