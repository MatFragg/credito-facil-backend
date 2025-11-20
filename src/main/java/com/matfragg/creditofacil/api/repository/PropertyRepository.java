package com.matfragg.creditofacil.api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matfragg.creditofacil.api.model.entities.Property;
import com.matfragg.creditofacil.api.model.enums.PropertyStatus;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByClientId(Long clientId);
    
    Page<Property> findByClientId(Long clientId, Pageable pageable);
    
    Optional<Property> findByPropertyCode(String propertyCode);

    List<Property> findByStatus(PropertyStatus status);
    
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
    
    List<Property> findByClientIdAndStatus(Long clientId, PropertyStatus status);

    List<Property> findByDistrictContainingIgnoreCase(String district);
    
    List<Property> findByProvinceContainingIgnoreCase(String province);
    
    List<Property> findByCityContainingIgnoreCase(String city);

    List<Property> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Property> findByBedroomsGreaterThanEqual(Integer bedrooms);

    List<Property> findByBathroomsGreaterThanEqual(Integer bathrooms);

    boolean existsByPropertyCode(String propertyCode);
    
    long countByClientId(Long clientId);
    
    long countByStatus(PropertyStatus status);
    
    @Query("SELECT COUNT(p) FROM Property p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(p.price) FROM Property p")
    BigDecimal avgPrice();
    
    @Query("SELECT MIN(p.price) FROM Property p")
    BigDecimal minPrice();
    
    @Query("SELECT MAX(p.price) FROM Property p")
    BigDecimal maxPrice();
    
    @Query("SELECT p.district, COUNT(p) FROM Property p GROUP BY p.district ORDER BY COUNT(p) DESC")
    List<Object[]> findMostPopularDistrict();
    
    @Query("SELECT p.province, COUNT(p) FROM Property p GROUP BY p.province ORDER BY COUNT(p) DESC")
    List<Object[]> findMostPopularProvince();

    @Query("SELECT COUNT(DISTINCT p.city) FROM Property p")
    long countDistinctCities();

    @Query("SELECT p.city, COUNT(p) FROM Property p GROUP BY p.city ORDER BY COUNT(p) DESC")
    List<Object[]> findMostPopularCity();
    
    @Query("SELECT AVG(p.bedrooms) FROM Property p")
    Double avgBedrooms();

    @Query("SELECT p.propertyType, COUNT(p) FROM Property p GROUP BY p.propertyType ORDER BY COUNT(p) DESC")
    List<Object[]> findMostPopularPropertyType();
    
    @Query("SELECT AVG(p.area) FROM Property p")
    BigDecimal avgArea();
}
