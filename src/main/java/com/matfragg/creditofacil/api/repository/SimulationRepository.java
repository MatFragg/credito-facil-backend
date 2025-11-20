package com.matfragg.creditofacil.api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matfragg.creditofacil.api.model.entities.Simulation;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    List<Simulation> findByClientId(Long clientId);
    
    Page<Simulation> findByClientId(Long clientId, Pageable pageable);

    List<Simulation> findByCreatedAtAfter(LocalDateTime date);
    
    List<Simulation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Simulation> findByBankEntityId(Long bankEntityId);

    List<Simulation> findByPropertyId(Long propertyId);

    List<Simulation> findByAnnualRateBetween(Double minRate, Double maxRate);

    long countByClientId(Long clientId);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Aggregation methods for statistics
    @Query("SELECT COUNT(s) FROM Simulation s")
    long countAllSimulations();
    
    @Query("SELECT SUM(s.amountToFinance) FROM Simulation s")
    BigDecimal sumFinancedAmount();
    
    @Query("SELECT AVG(s.monthlyPayment) FROM Simulation s")
    BigDecimal avgMonthlyPayment();
    
    @Query("SELECT AVG(s.propertyPrice) FROM Simulation s")
    BigDecimal avgPropertyPrice();
    
    @Query("SELECT AVG(s.downPayment) FROM Simulation s")
    BigDecimal avgDownPayment();
    
    @Query("SELECT AVG(s.loanTermMonths) FROM Simulation s")
    Double avgLoanYears();
    
    @Query("SELECT s.bankEntity.name, COUNT(s) FROM Simulation s GROUP BY s.bankEntity.name ORDER BY COUNT(s) DESC")
    List<Object[]> findMostUsedBank();
    
    // Client-specific statistics
    @Query("SELECT SUM(s.amountToFinance) FROM Simulation s WHERE s.client.id = :clientId")
    BigDecimal sumFinancedAmountByClient(@Param("clientId") Long clientId);
    
    @Query("SELECT AVG(s.monthlyPayment) FROM Simulation s WHERE s.client.id = :clientId")
    BigDecimal avgMonthlyPaymentByClient(@Param("clientId") Long clientId);
    
    @Query("SELECT AVG(s.propertyPrice) FROM Simulation s WHERE s.client.id = :clientId")
    BigDecimal avgPropertyPriceByClient(@Param("clientId") Long clientId);
    
    @Query("""
    SELECT b.id, b.name, b.currentRate, COUNT(s.id), 
           AVG(s.amountToFinance), AVG(s.monthlyPayment), 
           MIN(s.annualRate), MAX(s.annualRate), AVG(s.tcea)
    FROM Simulation s
    JOIN s.bankEntity b
    GROUP BY b.id, b.name, b.currentRate
    ORDER BY COUNT(s.id) DESC
    """)
    List<Object[]> getBankComparisonStats();

    @Query("""
    SELECT s.bankEntity.name, COUNT(s)
    FROM Simulation s
    WHERE s.client.id = :clientId
    GROUP BY s.bankEntity.name
    ORDER BY COUNT(s) DESC
    """)
    List<Object[]> findMostUsedBankByClientId(@Param("clientId") Long clientId);

    @Query("""
        SELECT s.property.propertyType, COUNT(s)
        FROM Simulation s
        WHERE s.client.id = :clientId
        GROUP BY s.property.propertyType
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> findMostPopularPropertyTypeByClientId(@Param("clientId") Long clientId);
}