package com.matfragg.creditofacil.api.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para errores de validación detallados
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    
    private LocalDateTime timestamp;
    
    private String error;
    
    private String message;
    
    private List<String> details;
    
    private String path;
}
