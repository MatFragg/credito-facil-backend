package com.matfragg.creditofacil.api.model.entities;

import com.matfragg.creditofacil.api.model.enums.PropertyStatus;
import com.matfragg.creditofacil.api.model.enums.PropertyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "property_name")
    private String propertyName;

    @Column(name = "project_name", length = 100)
    private String projectName;

    @Column(name = "property_code", unique = true)
    private String propertyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    private PropertyType propertyType;

    private BigDecimal price;

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "parking_spaces")
    private Integer parkingSpaces;

    @Column(name = "age_years")
    private Integer ageYears = 0;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "province", length = 100)
    private String province = "Lima";

    @Column(name = "department", length = 100)
    private String department = "Lima";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
