package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.PropertyRequest;
import com.matfragg.creditofacil.api.dto.response.PropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PropertyService {

    Page<PropertyResponse> findAll(Pageable pageable);

    PropertyResponse findById(Long id);
    PropertyResponse findByPropertyCode(String propertyCode);

    PropertyResponse create(PropertyRequest request);
    
    PropertyResponse create(PropertyRequest request, MultipartFile image);

    PropertyResponse update(Long id, PropertyRequest request);
    
    PropertyResponse update(Long id, PropertyRequest request, MultipartFile newImage);

    void delete(Long id);
    
    PropertyResponse updateImage(Long id, MultipartFile image);
    
    void deleteImage(Long id);

    Page<PropertyResponse> getMyProperties(Pageable pageable); // Propiedades del cliente autenticado
}
