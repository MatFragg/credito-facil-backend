package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.SimulationRequest;
import com.matfragg.creditofacil.api.dto.response.PaymentScheduleResponse;
import com.matfragg.creditofacil.api.dto.response.SimulationResponse;
import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.PaymentScheduleMapper;
import com.matfragg.creditofacil.api.mapper.SimulationMapper;
import com.matfragg.creditofacil.api.model.entities.*;
import com.matfragg.creditofacil.api.model.enums.BonusType;
import com.matfragg.creditofacil.api.model.enums.SimulationStatus;
import com.matfragg.creditofacil.api.repository.*;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.DownPaymentValidationService;
import com.matfragg.creditofacil.api.service.FinancialIndicatorsService;
import com.matfragg.creditofacil.api.service.FrenchMethodCalculatorService;
import com.matfragg.creditofacil.api.service.SimulationService;
import com.matfragg.creditofacil.api.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SimulationServiceImpl implements SimulationService {

    private final SimulationRepository simulationRepository;
    // PaymentScheduleRepository ya no es necesario - cronogramas se generan bajo demanda
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private final BankEntityRepository bankEntityRepository;
    private final SettingsRepository settingsRepository;
    private final SimulationMapper simulationMapper;
    private final PaymentScheduleMapper paymentScheduleMapper;
    private final FrenchMethodCalculatorService frenchMethodCalculator;
    private final FinancialIndicatorsService financialIndicatorsService;
    private final SecurityUtils securityUtils;
    private final DownPaymentValidationService downPaymentValidator;
    private final CurrencyService currencyService;

    @Override
    @Transactional(readOnly = true)
    public SimulationResponse calculate(SimulationRequest request) {
        log.debug("Calculando simulación para cliente: {}", request.getClientId());
        
        // 1. Validar que el cliente existe
        clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        
        // 2. Obtener entidad bancaria
        BankEntity bankEntity = bankEntityRepository.findById(request.getBankEntityId())
                .orElseThrow(() -> new ResourceNotFoundException("Entidad bancaria no encontrada"));
        
        // 3. Obtener propiedad para validar moneda
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        
        // 4. Obtener configuración (necesaria para determinar moneda de trabajo)
        Settings settings = settingsRepository.findById(request.getSettingsId())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));
        
        // ==================== MANEJO DE MONEDA ====================
        // Determinar la moneda de la simulación
        String simulationCurrency = request.getCurrency();
        if (simulationCurrency == null || simulationCurrency.isBlank()) {
            simulationCurrency = settings.getCurrency(); // Usar moneda del Settings
        }
        if (simulationCurrency == null || simulationCurrency.isBlank()) {
            simulationCurrency = "PEN"; // Default
        }
        
        // Obtener moneda de la propiedad
        String propertyCurrency = property.getCurrency();
        if (propertyCurrency == null || propertyCurrency.isBlank()) {
            propertyCurrency = "PEN";
        }
        
        // Variables para conversión
        BigDecimal exchangeRateUsed = BigDecimal.ONE;
        BigDecimal propertyPrice = request.getPropertyPrice();
        BigDecimal downPayment = request.getDownPayment();
        
        // Convertir si las monedas son diferentes
        if (!propertyCurrency.equals(simulationCurrency)) {
            exchangeRateUsed = currencyService.getExchangeRate(propertyCurrency, simulationCurrency);
            propertyPrice = currencyService.convert(propertyPrice, propertyCurrency, simulationCurrency);
            
            // Convertir cuota inicial si autoConvert está habilitado
            if (request.getAutoConvert() != null && request.getAutoConvert()) {
                downPayment = currencyService.convert(downPayment, propertyCurrency, simulationCurrency);
            }
            
            log.info("Conversión de moneda aplicada: {} {} -> {} {} (TC: {})", 
                    request.getPropertyPrice(), propertyCurrency,
                    propertyPrice, simulationCurrency, exchangeRateUsed);
        }
        
        String currencySymbol = currencyService.getCurrencySymbol(simulationCurrency);
        String alternateCurrency = currencyService.getAlternateCurrency(simulationCurrency);
        String alternateCurrencySymbol = currencyService.getCurrencySymbol(alternateCurrency);
        // ==================== FIN MANEJO DE MONEDA ====================
        
        // ==================== NUEVAS VALIDACIONES NCMV ====================
        // Nota: Las validaciones NCMV usan montos en PEN (convertir si es necesario)
        BigDecimal propertyPriceForNCMV = "PEN".equals(simulationCurrency) 
                ? propertyPrice 
                : currencyService.convert(propertyPrice, simulationCurrency, "PEN");
        BigDecimal downPaymentForNCMV = "PEN".equals(simulationCurrency) 
                ? downPayment 
                : currencyService.convert(downPayment, simulationCurrency, "PEN");
        
        // 5. VALIDAR RANGO DE VALOR DE VIVIENDA (NCMV) - en PEN
        if (bankEntity.getSupportsNCMV() != null && bankEntity.getSupportsNCMV()) {
            downPaymentValidator.validateNCMVPropertyRange(
                    propertyPriceForNCMV, 
                    bankEntity, 
                    false // useCRC - podría ser un parámetro del request
            );
        }
        
        // 6. VALIDAR CUOTA INICIAL MÍNIMA NCMV (7.5%) - en PEN
        downPaymentValidator.validateMinimumDownPaymentNCMV(
                propertyPriceForNCMV,
                downPaymentForNCMV
        );
        
        // ==================== FIN NUEVAS VALIDACIONES NCMV ====================
        
        // 5. VALIDAR CUOTA INICIAL MÍNIMA (reglas del banco)
        if (!downPaymentValidator.isDownPaymentValid(
                request.getPropertyPrice(), 
                request.getDownPayment(), 
                bankEntity)) {
            
            String errorMessage = downPaymentValidator.getDownPaymentErrorMessage(
                    request.getPropertyPrice(), 
                    request.getDownPayment(), 
                    bankEntity);
            
            throw new BadRequestException(errorMessage);
        }
        
        // 6. Calcular Bono Techo Propio (BFH)
        BigDecimal govBondAmount = BigDecimal.ZERO;
        if (request.getApplyGovernmentBonus() != null && request.getApplyGovernmentBonus()) {
            if (request.getPropertyPrice().compareTo(BigDecimal.valueOf(200000)) > 0) {
                throw new BadRequestException(
                        "El Bono Techo Propio solo aplica para viviendas menores o iguales a S/ 200,000");
            }
            
            if (request.getBonusType() == BonusType.ACQUISITION) {
                govBondAmount = BigDecimal.valueOf(37800);
            } else if (request.getBonusType() == BonusType.CONSTRUCTION) {
                govBondAmount = BigDecimal.valueOf(28400);
            } else if (request.getBonusType() == BonusType.IMPROVEMENT) {
                govBondAmount = BigDecimal.valueOf(18900);
            }
        }
        
        // ==================== NUEVO: CALCULAR PBP ====================
        BigDecimal pbpAmount = BigDecimal.ZERO;
        boolean applyPBP = request.getApplyPBP() != null && request.getApplyPBP();
        
        if (applyPBP) {
            pbpAmount = downPaymentValidator.calculatePBPAmount(request.getPropertyPrice(), bankEntity);
            if (pbpAmount.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("PBP solicitado pero la vivienda no califica. Precio: S/ {}", request.getPropertyPrice());
            } else {
                log.info("PBP aplicado: S/ {} para vivienda de S/ {}", pbpAmount, request.getPropertyPrice());
            }
        }
        // ==================== FIN PBP ====================
        
        // 7. Calcular monto a financiar (ahora incluye PBP)
        BigDecimal amountToFinance = propertyPrice
                .subtract(downPayment)
                .subtract(govBondAmount)
                .subtract(pbpAmount); // PBP reduce el saldo de deuda

        // ==================== CAPITALIZAR GASTOS INICIALES ====================
        // Los gastos iniciales se suman al monto a financiar (como en tu Excel)
        // "Monto del préstamo" = Saldo a financiar + Gastos iniciales
        BigDecimal initialCosts = request.getOpeningCommission()
                .add(request.getNotaryFees())
                .add(request.getRegistrationFees());
        
        // Monto del préstamo indexado (incluye gastos capitalizados)
        BigDecimal loanAmount = amountToFinance.add(initialCosts);
        
        log.info("Saldo a financiar: {}, Gastos capitalizados: {}, Monto préstamo: {}", 
                amountToFinance, initialCosts, loanAmount);
        // ==================== FIN CAPITALIZACIÓN ====================

        // 8. VALIDAR MONTO A FINANCIAR
        if (!downPaymentValidator.isFinancingAmountValid(
                request.getPropertyPrice(), 
                amountToFinance, 
                bankEntity)) {
            
            String errorMessage = downPaymentValidator.getFinancingAmountErrorMessage(
                    request.getPropertyPrice(), 
                    amountToFinance, 
                    bankEntity);
            
            throw new BadRequestException(errorMessage);
        }

        // 9. VALIDAR COHERENCIA DE MONTOS (actualizado para incluir PBP)
        BigDecimal totalBonuses = govBondAmount.add(pbpAmount);
        if (!downPaymentValidator.validateAmountCoherence(
                request.getPropertyPrice(),
                downPayment,
                amountToFinance,
                totalBonuses)) {
            
            throw new BadRequestException(
                    "Los montos proporcionados no suman correctamente el precio de la vivienda. " +
                    "Verifique: Precio = Cuota Inicial + Monto a Financiar + Bonos (BFH + PBP)");
        }
        
        // ==================== NUEVO: TASA DE DESGRAVAMEN ====================
        BigDecimal desgravamenRate = request.getDesgravamenRate();
        if (desgravamenRate == null) {
            desgravamenRate = bankEntity.getDesgravamenRate(); // Usar tasa del banco
        }
        // ==================== FIN DESGRAVAMEN ====================
        
        // ==================== SEGURO DE RIESGO ====================
        // Si se proporciona propertyInsuranceRate, se usa como porcentaje sobre el saldo
        // Si no, se usa propertyInsurance como monto fijo
        BigDecimal propertyInsuranceRate = request.getPropertyInsuranceRate();
        BigDecimal propertyInsuranceAmount = request.getPropertyInsurance();
        
        log.info("Seguro riesgo - Tasa: {}, Monto fijo: {}", propertyInsuranceRate, propertyInsuranceAmount);
        // ==================== FIN SEGURO DE RIESGO ====================
        
        // 11. Generar cronograma de pagos usando el monto del préstamo (con gastos capitalizados)
        List<PaymentSchedule> schedule = frenchMethodCalculator.calculatePaymentSchedule(
                loanAmount,  // ← CAMBIO: Usar monto con gastos capitalizados
                request.getAnnualRate(),
                request.getTermYears(),
                settings,
                request.getLifeInsuranceRate(),
                propertyInsuranceRate,   // Tasa de seguro riesgo (puede ser null)
                propertyInsuranceAmount, // Monto fijo (fallback)
                desgravamenRate // NUEVO PARÁMETRO
        );

        // 12. Obtener el pago mensual
        BigDecimal monthlyPayment;
        if (settings.getGraceMonths() != null && settings.getGraceMonths() > 0) {
            int firstOrdinaryMonth = Math.min(settings.getGraceMonths(), schedule.size() - 1);
            monthlyPayment = schedule.get(firstOrdinaryMonth).getPayment();
        } else {
            monthlyPayment = schedule.get(0).getPayment();
        }

        // 13. Calcular seguros y total mensual (usando loanAmount para seguros)
        BigDecimal lifeInsurance = request.getLifeInsuranceRate()
            .multiply(loanAmount)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Calcular seguro de riesgo del primer mes (para mostrar en response)
        BigDecimal propertyInsuranceFirst;
        if (propertyInsuranceRate != null && propertyInsuranceRate.compareTo(BigDecimal.ZERO) > 0) {
            // Si hay tasa, calcular sobre el saldo inicial
            propertyInsuranceFirst = loanAmount.multiply(propertyInsuranceRate)
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            // Si no, usar monto fijo
            propertyInsuranceFirst = propertyInsuranceAmount != null ? propertyInsuranceAmount : BigDecimal.ZERO;
        }
        
        // El desgravamen YA está incluido en la cuota (monthlyPayment)
        // porque se calculó con PAGO(TEP + desgravamen, n, PV)
        // Solo lo calculamos para mostrarlo por separado
        BigDecimal desgravamenFirst = desgravamenRate
            .multiply(loanAmount)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Flujo total = Cuota + SegRie + lifeInsurance
        // NO sumar desgravamen porque ya está en la cuota
        // Excel: Flujo = Cuota + PP + SegRie + Comision + Portes + GasAdm
        BigDecimal totalMonthlyPayment = monthlyPayment
            .add(lifeInsurance)
            .add(propertyInsuranceFirst);
        // NO incluir desgravamen - ya está en monthlyPayment

        // Los gastos adicionales ya están capitalizados, no se suman aparte para TCEA
        BigDecimal additionalCosts = initialCosts;

        // 14. Calcular indicadores financieros
        // Usar tasa de descuento del request o default 10%
        BigDecimal discountRate = request.getDiscountRate();
        if (discountRate == null) {
            discountRate = BigDecimal.TEN; // Default 10%
        }
        BigDecimal van = financialIndicatorsService.calculateVAN(loanAmount, schedule, discountRate);
        BigDecimal tir = financialIndicatorsService.calculateTIR(loanAmount, schedule);
        BigDecimal tcea = financialIndicatorsService.calculateTCEA(loanAmount, schedule, additionalCosts);

        // 15. Calcular totales
        int loanTermMonths = request.getTermYears() * 12;
        
        BigDecimal totalAmountToPay = schedule.stream()
            .map(PaymentSchedule::getTotalPayment)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterest = schedule.stream()
            .map(PaymentSchedule::getInterest)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLifeInsurance = schedule.stream()
            .map(PaymentSchedule::getLifeInsurance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPropertyInsurance = schedule.stream()
            .map(PaymentSchedule::getPropertyInsurance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // ==================== NUEVO: TOTAL DESGRAVAMEN ====================
        BigDecimal totalDesgravamen = schedule.stream()
            .map(ps -> ps.getDesgravamenInsurance() != null ? ps.getDesgravamenInsurance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // ==================== FIN TOTAL DESGRAVAMEN ====================

        log.info("Simulación calculada - Cuota: {} {}, Total: {} {}, PBP: S/ {}, Desgravamen Total: {} {}", 
                monthlyPayment, simulationCurrency, totalAmountToPay, simulationCurrency, 
                pbpAmount, totalDesgravamen, simulationCurrency);
        
        // Calcular montos alternativos para referencia
        BigDecimal propertyPriceAlternate = currencyService.convert(propertyPrice, simulationCurrency, alternateCurrency);
        BigDecimal monthlyPaymentAlternate = currencyService.convert(monthlyPayment, simulationCurrency, alternateCurrency);
        
        return SimulationResponse.builder()
                .clientId(request.getClientId())
                .propertyId(request.getPropertyId())
                .bankEntityId(request.getBankEntityId())
                .settingsId(request.getSettingsId())
                .simulationName(request.getSimulationName())
                .simulationCode("SIM-" + System.currentTimeMillis())
                // ==================== CAMPOS DE MONEDA ====================
                .currency(simulationCurrency)
                .currencySymbol(currencySymbol)
                .exchangeRateUsed(exchangeRateUsed)
                .propertyPriceAlternate(propertyPriceAlternate)
                .monthlyPaymentAlternate(monthlyPaymentAlternate)
                .alternateCurrency(alternateCurrency)
                .alternateCurrencySymbol(alternateCurrencySymbol)
                // ==================== FIN CAMPOS DE MONEDA ====================
                .propertyPrice(propertyPrice)
                .downPayment(downPayment)
                .amountToFinance(amountToFinance)
                .loanAmount(loanAmount)
                .applyGovernmentBonus(request.getApplyGovernmentBonus())
                .governmentBonusAmount(govBondAmount)
                .bonusType(request.getBonusType())
                // ==================== NUEVOS CAMPOS EN RESPONSE ====================
                .applyPBP(applyPBP)
                .pbpAmount(pbpAmount)
                .desgravamenRate(desgravamenRate)
                .totalDesgravamenInsurance(totalDesgravamen)
                // ==================== FIN NUEVOS CAMPOS ====================
                .annualRate(request.getAnnualRate())
                .termYears(request.getTermYears())
                .lifeInsuranceRate(request.getLifeInsuranceRate())
                .propertyInsurance(propertyInsuranceFirst) // Monto del primer mes (o fijo)
                .propertyInsuranceRate(propertyInsuranceRate) // Tasa si se usó porcentaje
                .openingCommission(request.getOpeningCommission())
                .notaryFees(request.getNotaryFees())
                .registrationFees(request.getRegistrationFees())
                .monthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP))
                .totalMonthlyPayment(totalMonthlyPayment.setScale(2, RoundingMode.HALF_UP)) 
                .totalAmountToPay(totalAmountToPay.setScale(2, RoundingMode.HALF_UP))
                .totalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP))
                .totalAdditionalCosts(additionalCosts)
                .loanTermMonths(loanTermMonths)
                .totalLifeInsurance(totalLifeInsurance.setScale(2, RoundingMode.HALF_UP))
                .totalPropertyInsurance(totalPropertyInsurance.setScale(2, RoundingMode.HALF_UP))
                .discountRate(discountRate)
                .npv(van)
                .irr(tir)
                .tcea(tcea)
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();
    }



    @Override
    public SimulationResponse save(SimulationRequest request) {
    log.debug("Guardando simulación completa con cronograma");

    // Primero calcular todo
    SimulationResponse calculated = calculate(request);

    // Crear entidad de simulación
    Simulation simulation = simulationMapper.toEntity(request);

    // Cargar y asignar entidades relacionadas
    Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    Property property = propertyRepository.findById(request.getPropertyId())
            .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
    BankEntity bankEntity = bankEntityRepository.findById(request.getBankEntityId())
            .orElseThrow(() -> new ResourceNotFoundException("Entidad bancaria no encontrada"));
    Settings settings = settingsRepository.findById(request.getSettingsId())
            .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));

    simulation.setClient(client);
    simulation.setProperty(property);
    simulation.setBankEntity(bankEntity);
    simulation.setSettings(settings);
    simulation.setCreatedAt(LocalDateTime.now());

    // ✅ ASIGNAR CÓDIGO DE SIMULACIÓN Y STATUS
    simulation.setSimulationCode(calculated.getSimulationCode());
    simulation.setStatus(request.getStatus() != null ? request.getStatus() : SimulationStatus.DRAFT);

    // ✅ ASIGNAR TODOS LOS CAMPOS CALCULADOS
    simulation.setAmountToFinance(calculated.getAmountToFinance());
    simulation.setLoanAmount(calculated.getLoanAmount()); // ✅ MONTO PRÉSTAMO (con gastos capitalizados)
    simulation.setGovernmentBonusAmount(calculated.getGovernmentBonusAmount()); // ✅ BONO
    simulation.setPbpAmount(calculated.getPbpAmount()); // ✅ PBP
    simulation.setApplyPBP(calculated.getApplyPBP()); // ✅ APPLY PBP
    simulation.setDiscountRate(calculated.getDiscountRate()); // ✅ TASA DESCUENTO
    simulation.setMonthlyPayment(calculated.getMonthlyPayment());
    simulation.setTotalMonthlyPayment(calculated.getTotalMonthlyPayment()); // ✅ TOTAL MENSUAL
    simulation.setTotalAmountToPay(calculated.getTotalAmountToPay()); // ✅ TOTAL A PAGAR
    simulation.setTotalInterest(calculated.getTotalInterest()); // ✅ INTERESES
    simulation.setTotalAdditionalCosts(calculated.getTotalAdditionalCosts()); // ✅ COSTOS ADICIONALES
    simulation.setLoanTermMonths(calculated.getLoanTermMonths()); // ✅ MESES
    simulation.setTotalLifeInsurance(calculated.getTotalLifeInsurance()); // ✅ SEGURO VIDA
    simulation.setTotalPropertyInsurance(calculated.getTotalPropertyInsurance()); // ✅ SEGURO PROPIEDAD
    simulation.setTotalDesgravamenInsurance(calculated.getTotalDesgravamenInsurance()); // ✅ DESGRAVAMEN TOTAL
    simulation.setPropertyInsuranceRate(calculated.getPropertyInsuranceRate()); // ✅ TASA SEGURO RIESGO
    simulation.setNpv(calculated.getNpv());
    simulation.setIrr(calculated.getIrr());
    simulation.setTcea(calculated.getTcea());
    
    // ✅ ASIGNAR CAMPOS DE MONEDA
    simulation.setCurrency(calculated.getCurrency());
    simulation.setExchangeRateUsed(calculated.getExchangeRateUsed());

    // Guardar simulación
    Simulation saved = simulationRepository.save(simulation);
    log.info("Simulación guardada con id: {} (cronograma se generará bajo demanda)", saved.getId());

    // ✅ NO guardar cronograma en DB - se genera bajo demanda
    // Esto ahorra ~300 registros por simulación de 25 años

    // Retornar el response calculado con el ID de la entidad guardada
    SimulationResponse response = simulationMapper.toResponse(saved);
    // Copiar los campos alternativos del cálculo original
    response.setPropertyPriceAlternate(calculated.getPropertyPriceAlternate());
    response.setMonthlyPaymentAlternate(calculated.getMonthlyPaymentAlternate());
    response.setAlternateCurrency(calculated.getAlternateCurrency());
    response.setAlternateCurrencySymbol(calculated.getAlternateCurrencySymbol());
    response.setCurrencySymbol(calculated.getCurrencySymbol());
    
    return response;
}

    @Override
    public SimulationResponse update(Long id, SimulationRequest request) {
        log.debug("Actualizando simulación con id: {}", id);

        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con id: " + id));

        // Actualizar campos básicos
        simulationMapper.updateEntityFromRequest(request, simulation);

        // Recalcular valores
        SimulationResponse recalculated = calculate(request);
        simulation.setAmountToFinance(recalculated.getAmountToFinance());
        simulation.setLoanAmount(recalculated.getLoanAmount()); // MONTO PRÉSTAMO
        simulation.setGovernmentBonusAmount(recalculated.getGovernmentBonusAmount());
        simulation.setPbpAmount(recalculated.getPbpAmount());
        simulation.setApplyPBP(recalculated.getApplyPBP());
        simulation.setDiscountRate(recalculated.getDiscountRate());
        simulation.setMonthlyPayment(recalculated.getMonthlyPayment());
        simulation.setTotalMonthlyPayment(recalculated.getTotalMonthlyPayment());
        simulation.setTotalAmountToPay(recalculated.getTotalAmountToPay());
        simulation.setTotalInterest(recalculated.getTotalInterest());
        simulation.setTotalAdditionalCosts(recalculated.getTotalAdditionalCosts());
        simulation.setLoanTermMonths(recalculated.getLoanTermMonths());
        simulation.setTotalLifeInsurance(recalculated.getTotalLifeInsurance());
        simulation.setTotalPropertyInsurance(recalculated.getTotalPropertyInsurance());
        simulation.setTotalDesgravamenInsurance(recalculated.getTotalDesgravamenInsurance());
        simulation.setTcea(recalculated.getTcea());
        simulation.setNpv(recalculated.getNpv());
        simulation.setIrr(recalculated.getIrr());
        simulation.setCurrency(recalculated.getCurrency());
        simulation.setExchangeRateUsed(recalculated.getExchangeRateUsed());
        simulation.setUpdatedAt(LocalDateTime.now());

        // Guardar simulación actualizada
        Simulation updated = simulationRepository.save(simulation);

        // ✅ NO actualizar cronograma en DB - se genera bajo demanda

        log.info("Simulación actualizada con id: {} (cronograma se regenerará bajo demanda)", id);
        return simulationMapper.toResponse(updated);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SimulationResponse> findAll(Pageable pageable) {
        log.debug("Buscando todas las simulaciones con paginación");
        return simulationRepository.findAll(pageable)
                .map(simulationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationResponse findById(Long id) {
        log.debug("Buscando simulación con id: {}", id);
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con id: " + id));
        return simulationMapper.toResponse(simulation);
    }

    @Override
    public void delete(Long id) {
        log.debug("Eliminando simulación con id: {}", id);

        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con id: " + id));

        // ✅ NO eliminar cronograma - ya no se almacena en DB

        // Eliminar simulación
        simulationRepository.delete(simulation);
        log.info("Simulación eliminada con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SimulationResponse> getMySimulations(Pageable pageable) {
        log.debug("Obteniendo TODAS las simulaciones del usuario autenticado");

        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        // ✅ Retorna TODAS las simulaciones del usuario con paginación
        Page<Simulation> simulations = simulationRepository.findByClientUserId(currentUser.getId(), pageable);
        
        log.debug("Encontradas {} simulaciones para el usuario {}", 
                simulations.getTotalElements(), currentUser.getEmail());

        return simulations.map(simulationMapper::toResponse);
    }

     @Override
    @Transactional(readOnly = true)
    public List<PaymentScheduleResponse> getSchedule(Long simulationId) {
        log.debug("Generando cronograma de pagos bajo demanda para simulación: {}", simulationId);

        // Obtener simulación con todos los datos necesarios
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con id: " + simulationId));

        Settings settings = simulation.getSettings();

        // ✅ CORRECCIÓN: Incluir desgravamenRate
        BigDecimal desgravamenRate = simulation.getDesgravamenRate();
        if (desgravamenRate == null) {
            desgravamenRate = BigDecimal.ZERO;
        }

        // Generar cronograma en tiempo real (no almacenado en DB)
        List<PaymentSchedule> schedule = frenchMethodCalculator.calculatePaymentSchedule(
                simulation.getLoanAmount() != null ? simulation.getLoanAmount() : simulation.getAmountToFinance(),
                simulation.getAnnualRate(),
                simulation.getTermYears(),
                settings,
                simulation.getLifeInsuranceRate(),
                simulation.getPropertyInsuranceRate(), // Tasa de seguro riesgo (puede ser null)
                simulation.getPropertyInsurance(),     // Monto fijo (fallback)
                desgravamenRate
        );

        log.debug("Cronograma de {} pagos generado exitosamente", schedule.size());

        return paymentScheduleMapper.toResponseList(schedule);
    }

    private void validateSimulationRequest(SimulationRequest request) {
        BigDecimal minDownPayment = request.getPropertyPrice()
                .multiply(new BigDecimal("0.10"));

        if (request.getDownPayment().compareTo(minDownPayment) < 0) {
            throw new BadRequestException(
                    "La cuota inicial debe ser al menos el 10% del precio de la propiedad"
            );
        }

        // Validar bono techo propio
        if (request.getApplyGovernmentBonus() != null && request.getApplyGovernmentBonus()) {
            if (request.getPropertyPrice().compareTo(new BigDecimal("200000")) > 0) {
                throw new BadRequestException(
                        "El Bono Techo Propio solo aplica para viviendas menores o iguales a S/ 200,000"
                );
            }
            if (request.getGovernmentBonusAmount() != null && 
                request.getGovernmentBonusAmount().compareTo(new BigDecimal("37800")) > 0) {
                throw new BadRequestException(
                        "El Bono Techo Propio no puede exceder S/ 37,800"
                );
            }
        }
    }
}