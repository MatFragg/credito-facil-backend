package com.matfragg.creditofacil.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matfragg.creditofacil.api.model.entities.Client;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    Page<Client> findByUserId(Long userId, Pageable pageable);

    List<Client> findAllByUserId(Long userId);

    Optional<Client> findByUserId(Long userId);

    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Client> findByUserIdAndUserIsNull(Long userId); 

    List<Client> findByUserIdAndUserIsNotNull(Long userId); 

    Optional<Client> findByDni(String dni);

    boolean existsByDni(String dni);

    long countByUserId(Long userId);

    long countByUserIsNull(); 

    long countByUserIsNotNull();

    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
