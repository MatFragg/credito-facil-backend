package com.matfragg.creditofacil.api.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements EnvironmentPostProcessor {

    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotEnvPropertySource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Cargar .env ignorando si no existe (para producción donde se usan variables de entorno reales)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        Map<String, Object> props = new HashMap<>();
        dotenv.entries().forEach(e -> props.put(e.getKey(), e.getValue()));
        
        if (!props.isEmpty()) {
            PropertySource<Map<String, Object>> propertySource = new MapPropertySource(DOTENV_PROPERTY_SOURCE_NAME, props);
            // Agregar al final para que las variables de entorno del sistema tengan prioridad sobre el archivo .env
            environment.getPropertySources().addLast(propertySource);
        }
    }
}
