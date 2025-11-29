package com.matfragg.creditofacil.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para Cloudflare Turnstile
 * Proporciona las propiedades y beans necesarios para la validación de tokens
 */
@Configuration
public class TurnstileConfig {

    @Value("${turnstile.secret-key:}")
    private String secretKey;

    @Value("${turnstile.verify-url:https://challenges.cloudflare.com/turnstile/v0/siteverify}")
    private String verifyUrl;

    @Value("${turnstile.enabled:true}")
    private boolean enabled;

    @Bean
    public RestTemplate turnstileRestTemplate() {
        return new RestTemplate();
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
