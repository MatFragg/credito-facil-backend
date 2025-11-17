package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.SettingsRequest;
import com.matfragg.creditofacil.api.dto.response.SettingsResponse;
import com.matfragg.creditofacil.api.model.entities.Settings;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SettingsMapper {

    @Mapping(target = "userId", source = "user.id")
    SettingsResponse toResponse(Settings settings);

    List<SettingsResponse> toResponseList(List<Settings> settings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Settings toEntity(SettingsRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(SettingsRequest request, @MappingTarget Settings settings);
}
