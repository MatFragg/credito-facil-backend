package com.matfragg.creditofacil.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para recibir la respuesta de Cloudflare Turnstile
 */
public class TurnstileResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    @JsonProperty("hostname")
    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    @JsonProperty("action")
    private String action;

    @JsonProperty("cdata")
    private String cdata;

    public TurnstileResponse() {
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getChallengeTs() {
        return challengeTs;
    }

    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCdata() {
        return cdata;
    }

    public void setCdata(String cdata) {
        this.cdata = cdata;
    }

    /**
     * Obtiene un mensaje de error legible basado en los códigos de error
     */
    public String getErrorMessage() {
        if (errorCodes == null || errorCodes.isEmpty()) {
            return "Error de validación desconocido";
        }

        StringBuilder message = new StringBuilder();
        for (String code : errorCodes) {
            switch (code) {
                case "missing-input-secret":
                    message.append("Falta la clave secreta. ");
                    break;
                case "invalid-input-secret":
                    message.append("La clave secreta es inválida. ");
                    break;
                case "missing-input-response":
                    message.append("Falta el token de respuesta. ");
                    break;
                case "invalid-input-response":
                    message.append("El token de respuesta es inválido o ha expirado. ");
                    break;
                case "invalid-widget-id":
                    message.append("El ID del widget es inválido. ");
                    break;
                case "invalid-parsed-secret":
                    message.append("La clave secreta no pudo ser parseada. ");
                    break;
                case "bad-request":
                    message.append("Solicitud malformada. ");
                    break;
                case "timeout-or-duplicate":
                    message.append("El token ha expirado o ya fue utilizado. ");
                    break;
                case "internal-error":
                    message.append("Error interno del servidor de Turnstile. ");
                    break;
                default:
                    message.append("Error: ").append(code).append(". ");
            }
        }
        return message.toString().trim();
    }
}
