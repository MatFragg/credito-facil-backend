package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.BankEntityRequest;
import com.matfragg.creditofacil.api.dto.response.BankEntityResponse;
import com.matfragg.creditofacil.api.model.entities.BankEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankEntityMapper {

    BankEntityResponse toResponse(BankEntity bankEntity);

    List<BankEntityResponse> toResponseList(List<BankEntity> bankEntities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    BankEntity toEntity(BankEntityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(BankEntityRequest request, @MappingTarget BankEntity bankEntity);
}
