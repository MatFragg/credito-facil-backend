package com.matfragg.creditofacil.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matfragg.creditofacil.api.model.entities.Settings;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}