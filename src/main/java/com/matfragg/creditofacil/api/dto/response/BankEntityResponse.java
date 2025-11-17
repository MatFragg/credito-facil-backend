package com.matfragg.creditofacil.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankEntityResponse {

    private Long id;
    private String name;
    private BigDecimal currentRate;
    private BigDecimal minimumIncome;
    private BigDecimal maxCoveragePct;
    private LocalDate lastUpdated;
}
