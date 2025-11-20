package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.RegisterRequest;
import com.matfragg.creditofacil.api.dto.request.UserProfileRequest;
import com.matfragg.creditofacil.api.dto.response.UserResponse;
import com.matfragg.creditofacil.api.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    /**
     * Convierte una entidad User a UserResponse DTO
     */
    @Mapping(target = "roles", source = "role")
    UserResponse toResponse(User user);
    
    /**
     * Convierte un RegisterRequest a una entidad User
     * Los campos como id, createdAt, lastLogin, isActive se manejan en el servicio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(RegisterRequest request);
    
    /**
     * Convierte una entidad User a UserProfileRequest DTO
     */
    UserProfileRequest toUserProfileRequest(User user);
    
    /**
     * Actualiza una entidad User existente desde un UserProfileRequest
     * Solo actualiza firstName, lastName y email, el resto se maneja en el servicio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "username", ignore = true)
    void updateUserFromRequest(UserProfileRequest request, @MappingTarget User user);
}

