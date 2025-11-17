package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.PropertyStatus;
import com.matfragg.creditofacil.api.model.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {

    private Long id;
    private Long clientId;
    private String propertyName;
    private String projectName;
    private String propertyCode;
    private PropertyType propertyType;
    private BigDecimal price;
    private BigDecimal area;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer parkingSpaces;
    private Integer ageYears;
    private String address;
    private String district;
    private String province;
    private String department;
    private PropertyStatus status;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
