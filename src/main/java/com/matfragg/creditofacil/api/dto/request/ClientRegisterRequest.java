package com.matfragg.creditofacil.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterRequest {
    
    // Datos de usuario
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    // Datos de cliente
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "DNI is required")
    @Pattern(regexp = "^\\d{8}$", message = "DNI must be 8 digits")
    private String dni;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must be 9 digits")
    private String phone;
    
    @NotNull(message = "Monthly income is required")
    @Positive(message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;
    
    private String occupation;
}