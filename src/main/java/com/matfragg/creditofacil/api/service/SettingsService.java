package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.SettingsRequest;
import com.matfragg.creditofacil.api.dto.response.SettingsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettingsService {

    Page<SettingsResponse> findAll(Pageable pageable);

    SettingsResponse findById(Long id);

    SettingsResponse create(SettingsRequest request);

    SettingsResponse update(Long id, SettingsRequest request);

    void delete(Long id);

    SettingsResponse getMySettings(); // Obtener configuración del usuario autenticado
}
