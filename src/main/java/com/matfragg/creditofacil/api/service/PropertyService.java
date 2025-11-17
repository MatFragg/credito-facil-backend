package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.PropertyRequest;
import com.matfragg.creditofacil.api.dto.response.PropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyService {

    Page<PropertyResponse> findAll(Pageable pageable);

    PropertyResponse findById(Long id);

    PropertyResponse create(PropertyRequest request);

    PropertyResponse update(Long id, PropertyRequest request);

    void delete(Long id);

    Page<PropertyResponse> getMyProperties(Pageable pageable); // Propiedades del cliente autenticado
}
