package com.matfragg.creditofacil.api.dto.request;

import com.matfragg.creditofacil.api.model.enums.EvaluationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @Pattern(regexp = "^\\d{8}$", message = "DNI must be 8 digits")
    private String dni;

    @Pattern(regexp = "^\\d{9}$", message = "Phone number must be 9 digits")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Positive(message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    @Pattern(regexp = "^(PEN|USD)$", message = "Currency must be PEN or USD")
    private String incomeCurrency = "PEN";

    private String occupation;

    private EvaluationStatus evaluationStatus;

    private String notes;
}
