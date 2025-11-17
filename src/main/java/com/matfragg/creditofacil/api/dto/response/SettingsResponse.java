package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.Capitalization;
import com.matfragg.creditofacil.api.model.enums.GracePeriodType;
import com.matfragg.creditofacil.api.model.enums.InterestRateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsResponse {

    private Long id;
    private Long userId;
    private String currency;
    private String language;
    private InterestRateType interestRateType;
    private Capitalization capitalization;
    private GracePeriodType gracePeriodType;
    private Integer graceMonths;
    private Boolean isCurrentSetting;
    private LocalDateTime createdAt;
}
