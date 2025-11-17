package com.matfragg.creditofacil.api.dto.request;

import com.matfragg.creditofacil.api.model.enums.Capitalization;
import com.matfragg.creditofacil.api.model.enums.GracePeriodType;
import com.matfragg.creditofacil.api.model.enums.InterestRateType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsRequest {

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(PEN|USD)$", message = "Currency must be PEN or USD")
    private String currency;

    @Size(max = 2, message = "Language code must be 2 characters")
    private String language;

    @NotNull(message = "Interest rate type is required")
    private InterestRateType interestRateType;

    private Capitalization capitalization;

    @NotNull(message = "Grace period type is required")
    private GracePeriodType gracePeriodType;

    @Min(value = 0, message = "Grace months cannot be negative")
    @Max(value = 60, message = "Grace months cannot exceed 60")
    private Integer graceMonths;

    private Boolean isCurrentSetting;
}
