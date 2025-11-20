package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.PeriodType;
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
public class PaymentScheduleResponse {

    private Integer paymentNumber;
    private LocalDate paymentDate;
    private BigDecimal initialBalance;
    private BigDecimal payment;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal finalBalance;
    private BigDecimal lifeInsurance;
    private BigDecimal propertyInsurance;
    private BigDecimal totalPayment;
    private PeriodType periodType;
}
