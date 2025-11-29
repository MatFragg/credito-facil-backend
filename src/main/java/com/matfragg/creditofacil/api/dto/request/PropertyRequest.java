package com.matfragg.creditofacil.api.dto.request;

import com.matfragg.creditofacil.api.model.enums.PropertyStatus;
import com.matfragg.creditofacil.api.model.enums.PropertyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @Size(max = 100, message = "Property name cannot exceed 100 characters")
    private String propertyName;

    @Size(max = 200, message = "Project name cannot exceed 200 characters")
    private String projectName;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Pattern(regexp = "^(PEN|USD)$", message = "Currency must be PEN or USD")
    @Builder.Default
    private String currency = "PEN";

    @DecimalMin(value = "0.01", message = "Area must be greater than 0")
    private BigDecimal area;

    @Min(value = 0, message = "Bedrooms must be at least 0")
    private Integer bedrooms;

    @Min(value = 0, message = "Bathrooms must be at least 0")
    private Integer bathrooms;

    @Min(value = 0, message = "Parking spaces must be at least 0")
    private Integer parkingSpaces;

    @Min(value = 0, message = "Age must be at least 0")
    private Integer ageYears;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;

    @Size(max = 100, message = "Province cannot exceed 100 characters")
    private String province;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    private PropertyStatus status;

    private String description;

    private Boolean isEcoFriendly;
}
