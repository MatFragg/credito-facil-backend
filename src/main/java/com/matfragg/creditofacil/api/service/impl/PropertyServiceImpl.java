package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.PropertyRequest;
import com.matfragg.creditofacil.api.dto.response.PropertyResponse;
import com.matfragg.creditofacil.api.dto.response.UploadResponse;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.PropertyMapper;
import com.matfragg.creditofacil.api.model.entities.Client;
import com.matfragg.creditofacil.api.model.entities.Property;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.ClientRepository;
import com.matfragg.creditofacil.api.repository.PropertyRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.PropertyService;
import com.matfragg.creditofacil.api.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final ClientRepository clientRepository;
    private final PropertyMapper propertyMapper;
    private final SecurityUtils securityUtils;
    private final StorageService storageService;

    @Value("${storage.cloudinary.folder:creditofacil/properties}")
    private String uploadFolder; 

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las propiedades con paginación");
        return propertyRepository.findAll(pageable)
                .map(propertyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse findById(Long id) {
        log.debug("Buscando propiedad con id: {}", id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));
        return propertyMapper.toResponse(property);
    }

    @Override
    public PropertyResponse findByPropertyCode(String propertyCode) {
        Property property = propertyRepository.findByPropertyCode(propertyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con propertyCode: " + propertyCode));
        return propertyMapper.toResponse(property);
    }

    @Override
    public PropertyResponse create(PropertyRequest request) {
        return create(request, null);
    }

    @Override
    public PropertyResponse create(PropertyRequest request, MultipartFile image) {
        log.debug("Creando nueva propiedad");

        // Validar cliente
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getClientId()));

        Property property = propertyMapper.toEntity(request);
        property.setClient(client);
        property.setPropertyCode(generatePropertyCode());

        // Upload image if provided
        if (image != null && !image.isEmpty()) {
            UploadResponse uploadResponse = storageService.upload(image, uploadFolder);
            property.setImageUrl(uploadResponse.getUrl());
            property.setImagePublicId(uploadResponse.getPublicId());
            log.debug("Imagen subida exitosamente: {}", uploadResponse.getPublicId());
        }

        Property saved = propertyRepository.save(property);
        log.info("Propiedad creada exitosamente con id: {}", saved.getId());
        
        return propertyMapper.toResponse(saved);
    }

    @Override
    public PropertyResponse update(Long id, PropertyRequest request) {
        return update(id, request, null);
    }

    @Override
    public PropertyResponse update(Long id, PropertyRequest request, MultipartFile newImage) {
        log.debug("Actualizando propiedad con id: {}", id);
        
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Delete old image if a new one is provided
        if (newImage != null && !newImage.isEmpty()) {
            if (property.getImagePublicId() != null) {
                storageService.delete(property.getImagePublicId());
                log.debug("Imagen anterior eliminada: {}", property.getImagePublicId());
            }

            // Upload new image
            UploadResponse uploadResponse = storageService.upload(newImage, uploadFolder);
            property.setImageUrl(uploadResponse.getUrl());
            property.setImagePublicId(uploadResponse.getPublicId());
            log.debug("Nueva imagen subida exitosamente: {}", uploadResponse.getPublicId());
        }

        // Actualizar campos básicos
        propertyMapper.updateEntityFromRequest(request, property);

        Property updated = propertyRepository.save(property);
        log.info("Propiedad actualizada exitosamente con id: {}", id);
        
        return propertyMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        log.debug("Eliminando propiedad con id: {}", id);
        
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Delete image from Cloudinary if exists
        if (property.getImagePublicId() != null) {
            storageService.delete(property.getImagePublicId());
            log.debug("Imagen eliminada de Cloudinary: {}", property.getImagePublicId());
        }

        // TODO: Validar que no tenga simulaciones asociadas antes de eliminar
        
        propertyRepository.delete(property);
        log.info("Propiedad eliminada exitosamente con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getMyProperties(Pageable pageable) {
        log.debug("Obteniendo TODAS las propiedades del usuario autenticado");

        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        // ✅ Retorna TODAS las propiedades del usuario con paginación
        Page<Property> properties = propertyRepository.findByClientUserId(currentUser.getId(), pageable);
        
        log.debug("Encontradas {} propiedades para el usuario {}", 
                properties.getTotalElements(), currentUser.getEmail());

        return properties.map(propertyMapper::toResponse);
    }

    @Override
    public PropertyResponse updateImage(Long id, MultipartFile image) {
        log.debug("Actualizando imagen de propiedad con id: {}", id);
        
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Delete old image if exists
        if (property.getImagePublicId() != null) {
            storageService.delete(property.getImagePublicId());
            log.debug("Imagen anterior eliminada: {}", property.getImagePublicId());
        }

        // Upload new image
        UploadResponse uploadResponse = storageService.upload(image, uploadFolder);
        property.setImageUrl(uploadResponse.getUrl());
        property.setImagePublicId(uploadResponse.getPublicId());

        Property updated = propertyRepository.save(property);
        log.info("Imagen de propiedad actualizada exitosamente: {}", id);
        
        return propertyMapper.toResponse(updated);
    }

    @Override
    public void deleteImage(Long id) {
        log.debug("Eliminando imagen de propiedad con id: {}", id);
        
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        if (property.getImagePublicId() != null) {
            storageService.delete(property.getImagePublicId());
            property.setImageUrl(null);
            property.setImagePublicId(null);
            propertyRepository.save(property);
            log.info("Imagen eliminada exitosamente de la propiedad: {}", id);
        }
    }

    // ============ Métodos Privados ============

    private String generatePropertyCode() {
        long count = propertyRepository.count();
        String code;
        do {
            count++;
            code = String.format("PROP-%05d", count);
        } while (propertyRepository.existsByPropertyCode(code));
        
        log.debug("Código de propiedad generado: {}", code);
        return code;
    }
}
