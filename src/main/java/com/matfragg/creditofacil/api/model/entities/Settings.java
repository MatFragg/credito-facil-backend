package com.matfragg.creditofacil.api.model.entities;

import com.matfragg.creditofacil.api.model.enums.Capitalization;
import com.matfragg.creditofacil.api.model.enums.GracePeriodType;
import com.matfragg.creditofacil.api.model.enums.InterestRateType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "PEN";

    @Column(name = "language", length = 2)
    private String language = "es";

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_rate_type")
    private InterestRateType interestRateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "capitalization")
    private Capitalization capitalization;

    @Enumerated(EnumType.STRING)
    @Column(name = "grace_period_type")
    private GracePeriodType gracePeriodType;

    @Column(name = "grace_months", nullable = false)
    private Integer graceMonths = 0;

    @Column(name = "is_current_setting")
    private Boolean isCurrentSetting = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
