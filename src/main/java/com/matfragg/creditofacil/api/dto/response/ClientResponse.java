package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.EvaluationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String phone;
    private String email;
    private BigDecimal monthlyIncome;
    private String occupation;
    private EvaluationStatus evaluationStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
