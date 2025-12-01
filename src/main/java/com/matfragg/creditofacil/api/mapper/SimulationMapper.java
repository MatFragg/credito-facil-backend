package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.request.SimulationRequest;
import com.matfragg.creditofacil.api.dto.response.SimulationResponse;
import com.matfragg.creditofacil.api.model.entities.Simulation;
import com.matfragg.creditofacil.api.service.CurrencyService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SimulationMapper {

    @Autowired
    protected CurrencyService currencyService;

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "bankEntityId", source = "bankEntity.id")
    @Mapping(target = "settingsId", source = "settings.id")
    @Mapping(target = "currencySymbol", ignore = true)
    @Mapping(target = "propertyPriceAlternate", ignore = true)
    @Mapping(target = "monthlyPaymentAlternate", ignore = true)
    @Mapping(target = "alternateCurrency", ignore = true)
    @Mapping(target = "alternateCurrencySymbol", ignore = true)
    public abstract SimulationResponse toResponse(Simulation simulation);

    @AfterMapping
    protected void enrichWithCurrencyData(Simulation simulation, @MappingTarget SimulationResponse response) {
        if (currencyService == null) return;
        
        String currency = simulation.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = "PEN";
        }
        
        // Calcular símbolo de moneda
        response.setCurrencySymbol(currencyService.getCurrencySymbol(currency));
        
        // Calcular moneda alternativa
        String alternateCurrency = currencyService.getAlternateCurrency(currency);
        response.setAlternateCurrency(alternateCurrency);
        response.setAlternateCurrencySymbol(currencyService.getCurrencySymbol(alternateCurrency));
        
        // Calcular montos alternativos
        BigDecimal propertyPrice = simulation.getPropertyPrice();
        BigDecimal monthlyPayment = simulation.getMonthlyPayment();
        
        if (propertyPrice != null) {
            response.setPropertyPriceAlternate(
                currencyService.convert(propertyPrice, currency, alternateCurrency)
            );
        }
        
        if (monthlyPayment != null) {
            response.setMonthlyPaymentAlternate(
                currencyService.convert(monthlyPayment, currency, alternateCurrency)
            );
        }
    }

    public abstract List<SimulationResponse> toResponseList(List<Simulation> simulations);

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
    @Mapping(target = "loanAmount", ignore = true)
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
    public abstract Simulation toEntity(SimulationRequest request);

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
    @Mapping(target = "loanAmount", ignore = true)
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
    public abstract void updateEntityFromRequest(SimulationRequest request, @MappingTarget Simulation simulation);
}
