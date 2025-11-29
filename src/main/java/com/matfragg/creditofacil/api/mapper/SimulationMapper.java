package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.SimulationRequest;
import com.matfragg.creditofacil.api.dto.response.SimulationResponse;
import com.matfragg.creditofacil.api.model.entities.Simulation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SimulationMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "bankEntityId", source = "bankEntity.id")
    @Mapping(target = "settingsId", source = "settings.id")
    @Mapping(target = "currencySymbol", ignore = true)
    @Mapping(target = "propertyPriceAlternate", ignore = true)
    @Mapping(target = "monthlyPaymentAlternate", ignore = true)
    @Mapping(target = "alternateCurrency", ignore = true)
    @Mapping(target = "alternateCurrencySymbol", ignore = true)
    SimulationResponse toResponse(Simulation simulation);

    List<SimulationResponse> toResponseList(List<Simulation> simulations);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "bankEntity", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "simulationCode", ignore = true)
    @Mapping(target = "exchangeRateUsed", ignore = true)
    @Mapping(target = "pbpAmount", ignore = true)
    @Mapping(target = "totalDesgravamenInsurance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "amountToFinance", ignore = true)
    @Mapping(target = "monthlyPayment", ignore = true)
    @Mapping(target = "totalMonthlyPayment", ignore = true)
    @Mapping(target = "totalAmountToPay", ignore = true)
    @Mapping(target = "totalInterest", ignore = true)
    @Mapping(target = "totalAdditionalCosts", ignore = true)
    @Mapping(target = "loanTermMonths", ignore = true)
    @Mapping(target = "totalLifeInsurance", ignore = true)
    @Mapping(target = "totalPropertyInsurance", ignore = true)
    @Mapping(target = "npv", ignore = true)
    @Mapping(target = "irr", ignore = true)
    @Mapping(target = "tcea", ignore = true)
    Simulation toEntity(SimulationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "bankEntity", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "simulationCode", ignore = true)
    @Mapping(target = "exchangeRateUsed", ignore = true)
    @Mapping(target = "pbpAmount", ignore = true)
    @Mapping(target = "totalDesgravamenInsurance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "amountToFinance", ignore = true)
    @Mapping(target = "monthlyPayment", ignore = true)
    @Mapping(target = "totalMonthlyPayment", ignore = true)
    @Mapping(target = "totalAmountToPay", ignore = true)
    @Mapping(target = "totalInterest", ignore = true)
    @Mapping(target = "totalAdditionalCosts", ignore = true)
    @Mapping(target = "loanTermMonths", ignore = true)
    @Mapping(target = "totalLifeInsurance", ignore = true)
    @Mapping(target = "totalPropertyInsurance", ignore = true)
    @Mapping(target = "npv", ignore = true)
    @Mapping(target = "irr", ignore = true)
    @Mapping(target = "tcea", ignore = true)
    void updateEntityFromRequest(SimulationRequest request, @MappingTarget Simulation simulation);
}
