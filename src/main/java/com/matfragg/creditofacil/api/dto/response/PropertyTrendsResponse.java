package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyTrendsResponse {
    
    private String priceRange; // "0-100000", "100000-200000", etc.
    private Long propertiesCount;
    private String mostPopularCity;
    private Long citiesCount;
    private String mostPopularDistrict;
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer averageBedrooms;
    private BigDecimal averageArea;
}
