package com.matfragg.creditofacil.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para enviar la solicitud de verificación a Cloudflare Turnstile
 */
public class TurnstileTokenRequest {

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("response")
    private String response;

    @JsonProperty("remoteip")
    private String remoteIp;

    public TurnstileTokenRequest() {
    }

    public TurnstileTokenRequest(String secret, String response, String remoteIp) {
        this.secret = secret;
        this.response = response;
        this.remoteIp = remoteIp;
    }

    // Getters y Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
}
