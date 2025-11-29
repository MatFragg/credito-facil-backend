package com.matfragg.creditofacil.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.matfragg.creditofacil.api.model.entities.Settings;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Page<Settings> findByUserId(Long userId, Pageable pageable);

    Optional<Settings> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}