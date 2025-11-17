package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.SettingsRequest;
import com.matfragg.creditofacil.api.dto.response.SettingsResponse;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.SettingsMapper;
import com.matfragg.creditofacil.api.model.entities.Settings;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.SettingsRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsRepository settingsRepository;
    private final SettingsMapper settingsMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<SettingsResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las configuraciones con paginación");
        return settingsRepository.findAll(pageable)
                .map(settingsMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsResponse findById(Long id) {
        log.debug("Buscando configuración con id: {}", id);
        Settings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada con id: " + id));
        return settingsMapper.toResponse(settings);
    }

    @Override
    public SettingsResponse create(SettingsRequest request) {
        log.debug("Creando nueva configuración");

        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        Settings settings = settingsMapper.toEntity(request);
        settings.setUser(currentUser);  // <-- Pasa el objeto User

        Settings saved = settingsRepository.save(settings);
        log.info("Configuración creada exitosamente con id: {}", saved.getId());

        return settingsMapper.toResponse(saved);
    }

    @Override
    public SettingsResponse update(Long id, SettingsRequest request) {
        log.debug("Actualizando configuración con id: {}", id);

        Settings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada con id: " + id));

        settingsMapper.updateEntityFromRequest(request, settings);

        Settings updated = settingsRepository.save(settings);
        log.info("Configuración actualizada exitosamente con id: {}", id);

        return settingsMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        log.debug("Eliminando configuración con id: {}", id);

        Settings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada con id: " + id));

        settingsRepository.delete(settings);
        log.info("Configuración eliminada exitosamente con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SettingsResponse getMySettings() {
        log.debug("Obteniendo configuración del usuario autenticado");

        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        Settings settings = settingsRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró configuración para el usuario actual"));

        return settingsMapper.toResponse(settings);
    }
}
