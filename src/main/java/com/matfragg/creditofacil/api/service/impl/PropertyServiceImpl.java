package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.PropertyRequest;
import com.matfragg.creditofacil.api.dto.response.PropertyResponse;
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
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final ClientRepository clientRepository;
    private final PropertyMapper propertyMapper;
    private final SecurityUtils securityUtils; 

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
    public PropertyResponse create(PropertyRequest request) {
        log.debug("Creando nueva propiedad");

        // Validar cliente
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getClientId()));

        Property property = propertyMapper.toEntity(request);
        property.setClient(client);
        property.setPropertyCode(generatePropertyCode());

        Property saved = propertyRepository.save(property);
        log.info("Propiedad creada exitosamente con id: {}", saved.getId());
        
        return propertyMapper.toResponse(saved);
    }

    @Override
    public PropertyResponse update(Long id, PropertyRequest request) {
        log.debug("Actualizando propiedad con id: {}", id);
        
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

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

        // TODO: Validar que no tenga simulaciones asociadas antes de eliminar
        
        propertyRepository.delete(property);
        log.info("Propiedad eliminada exitosamente con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getMyProperties(Pageable pageable) {
        log.debug("Obteniendo propiedades del cliente autenticado");
        
        User currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));
        
        // Buscar el cliente asociado al usuario actual
        Client client = clientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario actual"));
        
        return propertyRepository.findByClientId(client.getId(), pageable)
                .map(propertyMapper::toResponse);
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
