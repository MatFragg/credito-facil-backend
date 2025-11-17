package com.matfragg.creditofacil.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matfragg.creditofacil.api.model.entities.BankEntity;

public interface BankEntityRepository extends JpaRepository<BankEntity, Long> {
    Optional<BankEntity> findByNameContainingIgnoreCase(String name);
    
    List<BankEntity> findByMinimumIncomeLessThanEqual(Double income);

    List<BankEntity> findByCurrentRateBetween(Double minRate, Double maxRate);

    List<BankEntity> findByMaxCoveragePctGreaterThanEqual(Double coveragePct);

    boolean existsByName(String name);
}