package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.ClientRequest;
import com.matfragg.creditofacil.api.dto.response.ClientResponse;
import com.matfragg.creditofacil.api.model.entities.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    
    /**
     * Convierte una entidad Client a ClientResponse DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "incomeCurrencySymbol", ignore = true)
    ClientResponse toResponse(Client client);
    
    /**
     * Convierte un ClientRequest a una entidad Client
     * El campo user se maneja en el servicio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientRequest request);
    
    /**
     * Actualiza una entidad Client existente desde un ClientRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ClientRequest request, @MappingTarget Client client);
}
