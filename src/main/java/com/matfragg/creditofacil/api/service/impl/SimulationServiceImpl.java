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
import com.matfragg.creditofacil.api.repository.*;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.FinancialIndicatorsService;
import com.matfragg.creditofacil.api.service.FrenchMethodCalculatorService;
import com.matfragg.creditofacil.api.service.SimulationService;
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
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private final BankEntityRepository bankEntityRepository;
    private final SettingsRepository settingsRepository;
    private final SimulationMapper simulationMapper;
    private final PaymentScheduleMapper paymentScheduleMapper;
    private final FrenchMethodCalculatorService frenchMethodCalculator;
    private final FinancialIndicatorsService financialIndicatorsService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public SimulationResponse calculate(SimulationRequest request) {
        // Validar que el cliente existe
        clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        
        // Validar bono gubernamental
        if (request.getApplyGovernmentBonus() != null && request.getApplyGovernmentBonus() && request.getPropertyPrice().compareTo(BigDecimal.valueOf(200000)) > 0) {
            throw new BadRequestException("El Bono Techo Propio solo aplica para viviendas menores o iguales a S/ 200,000");
        }

        // 1. Calcular downPayment
        BigDecimal downPayment = request.getDownPayment();

        // 2. Calcular bono gubernamental PRIMERO
        BigDecimal govBondAmount = BigDecimal.ZERO;
        if (request.getApplyGovernmentBonus() != null && request.getApplyGovernmentBonus()) {
            if (request.getBonusType() == BonusType.ACQUISITION) {
                govBondAmount = BigDecimal.valueOf(37800); // Max S/ 37,800
            } else if (request.getBonusType() == BonusType.CONSTRUCTION) {
                govBondAmount = BigDecimal.valueOf(28400); // Max S/ 28,400
            } else if (request.getBonusType() == BonusType.IMPROVEMENT) {
                govBondAmount = BigDecimal.valueOf(18900); // Max S/ 18,900
            }
        }
        
        // 3. Calcular amountToFinance RESTANDO EL BONO
        BigDecimal amountToFinance = request.getPropertyPrice()
                .subtract(downPayment)
                .subtract(govBondAmount);

        // Obtener settings
        Settings settings = settingsRepository.findById(request.getSettingsId())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));

        // Obtener BankEntity
        bankEntityRepository.findById(request.getBankEntityId())
                .orElseThrow(() -> new ResourceNotFoundException("Entidad bancaria no encontrada"));
        
        // Generar cronograma de pagos usando el método francés
        List<PaymentSchedule> schedule = frenchMethodCalculator.calculatePaymentSchedule(
                amountToFinance,
                request.getAnnualRate(), // Pasa la tasa original (ej. 9.5)
                request.getTermYears(),
                settings, // Pasa el objeto Settings completo
                request.getLifeInsuranceRate(),
                request.getPropertyInsurance()
        );

        // Obtener el pago mensual del primer mes del cronograma
        BigDecimal monthlyPayment;
        if (settings.getGraceMonths() != null && settings.getGraceMonths() > 0) {
            int firstOrdinaryMonth = Math.min(settings.getGraceMonths(), schedule.size() - 1);
            monthlyPayment = schedule.get(firstOrdinaryMonth).getPayment();
        } else {
            monthlyPayment = schedule.get(0).getPayment();
        }

        // ✅ CORRECCIÓN: Calcular totalMonthlyPayment correctamente
        BigDecimal lifeInsurance = request.getLifeInsuranceRate()
            .multiply(amountToFinance)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal propertyInsurance = request.getPropertyInsurance();
        BigDecimal totalMonthlyPayment = monthlyPayment
            .add(lifeInsurance)
            .add(propertyInsurance);

        BigDecimal additionalCosts = request.getOpeningCommission()
        .add(request.getNotaryFees())
        .add(request.getRegistrationFees());

        // Calcular indicadores financieros
        BigDecimal van = financialIndicatorsService.calculateVAN(amountToFinance, schedule, null);
        BigDecimal tir = financialIndicatorsService.calculateTIR(amountToFinance, schedule);
        BigDecimal tcea = financialIndicatorsService.calculateTCEA(amountToFinance, schedule, additionalCosts);

        // Calcular totales adicionales
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

        // Construir y retornar SimulationResponse
        return SimulationResponse.builder()
                .clientId(request.getClientId())
                .propertyId(request.getPropertyId())
                .bankEntityId(request.getBankEntityId())
                .settingsId(request.getSettingsId())
                .simulationName(request.getSimulationName())
                .simulationCode("SIM-" + System.currentTimeMillis())
                .propertyPrice(request.getPropertyPrice())
                .downPayment(downPayment)
                .amountToFinance(amountToFinance)
                .applyGovernmentBonus(request.getApplyGovernmentBonus())
                .governmentBonusAmount(govBondAmount)
                .bonusType(request.getBonusType())
                .annualRate(request.getAnnualRate())
                .termYears(request.getTermYears())
                .lifeInsuranceRate(request.getLifeInsuranceRate())
                .propertyInsurance(request.getPropertyInsurance())
                .openingCommission(request.getOpeningCommission())
                .notaryFees(request.getNotaryFees())
                .registrationFees(request.getRegistrationFees())
                .monthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP))
                .totalMonthlyPayment(totalMonthlyPayment.setScale(2, RoundingMode.HALF_UP)) // ✅ CORREGIDO
                .totalAmountToPay(totalAmountToPay.setScale(2, RoundingMode.HALF_UP))
                .totalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP))
                .totalAdditionalCosts(additionalCosts)
                .loanTermMonths(loanTermMonths)
                .totalLifeInsurance(totalLifeInsurance.setScale(2, RoundingMode.HALF_UP))
                .totalPropertyInsurance(totalPropertyInsurance.setScale(2, RoundingMode.HALF_UP))
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

    // ✅ ASIGNAR TODOS LOS CAMPOS CALCULADOS
    simulation.setAmountToFinance(calculated.getAmountToFinance());
    simulation.setGovernmentBonusAmount(calculated.getGovernmentBonusAmount()); // ✅ BONO
    simulation.setMonthlyPayment(calculated.getMonthlyPayment());
    simulation.setTotalMonthlyPayment(calculated.getTotalMonthlyPayment()); // ✅ TOTAL MENSUAL
    simulation.setTotalAmountToPay(calculated.getTotalAmountToPay()); // ✅ TOTAL A PAGAR
    simulation.setTotalInterest(calculated.getTotalInterest()); // ✅ INTERESES
    simulation.setTotalAdditionalCosts(calculated.getTotalAdditionalCosts()); // ✅ COSTOS ADICIONALES
    simulation.setLoanTermMonths(calculated.getLoanTermMonths()); // ✅ MESES
    simulation.setTotalLifeInsurance(calculated.getTotalLifeInsurance()); // ✅ SEGURO VIDA
    simulation.setTotalPropertyInsurance(calculated.getTotalPropertyInsurance()); // ✅ SEGURO PROPIEDAD
    simulation.setNpv(calculated.getNpv());
    simulation.setIrr(calculated.getIrr());
    simulation.setTcea(calculated.getTcea());

    // Guardar simulación
    Simulation saved = simulationRepository.save(simulation);
    log.info("Simulación guardada con id: {}", saved.getId());

    // Calcular y guardar cronograma
    List<PaymentSchedule> schedule = frenchMethodCalculator.calculatePaymentSchedule(
            saved.getAmountToFinance(),
            saved.getAnnualRate(),
            saved.getTermYears(),
            settings,
            saved.getLifeInsuranceRate(),
            saved.getPropertyInsurance()
    );

    // Asociar simulación a cada pago
    schedule.forEach(payment -> payment.setSimulation(saved));

    // Guardar cronograma
    paymentScheduleRepository.saveAll(schedule);
    log.info("Cronograma de {} pagos guardado para simulación {}", schedule.size(), saved.getId());

    return simulationMapper.toResponse(saved);
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
        simulation.setMonthlyPayment(recalculated.getMonthlyPayment());
        simulation.setTcea(recalculated.getTcea());
        simulation.setNpv(recalculated.getNpv());
        simulation.setIrr(recalculated.getIrr());

        // Guardar
        Simulation updated = simulationRepository.save(simulation);

        // Eliminar cronograma antiguo y crear uno nuevo
        paymentScheduleRepository.deleteAll(
                paymentScheduleRepository.findBySimulationId(id)
        );

        Settings settings = settingsRepository.findById(request.getSettingsId())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));

        List<PaymentSchedule> newSchedule = frenchMethodCalculator.calculatePaymentSchedule(
                updated.getAmountToFinance(),
                updated.getAnnualRate(),
                updated.getTermYears(),
                settings,
                updated.getLifeInsuranceRate(),
                updated.getPropertyInsurance()
        );

        newSchedule.forEach(payment -> payment.setSimulation(updated));
        paymentScheduleRepository.saveAll(newSchedule);

        log.info("Simulación actualizada con id: {}", id);
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

        // Eliminar cronograma primero
        paymentScheduleRepository.deleteAll(
                paymentScheduleRepository.findBySimulationId(id)
        );

        // Eliminar simulación
        simulationRepository.delete(simulation);
        log.info("Simulación eliminada con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SimulationResponse> getMySimulations(Pageable pageable) {
        log.debug("Obteniendo simulaciones del usuario autenticado");

        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

        // Buscar cliente asociado al usuario
        Client client = clientRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario actual"));

        return simulationRepository.findByClientId(client.getId(), pageable)
                .map(simulationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentScheduleResponse> getSchedule(Long simulationId) {
        log.debug("Obteniendo cronograma de pagos para simulación: {}", simulationId);

        // Verificar que la simulación existe
        if (!simulationRepository.existsById(simulationId)) {
            throw new ResourceNotFoundException("Simulación no encontrada con id: " + simulationId);
        }

        List<PaymentSchedule> schedule = paymentScheduleRepository
                .findBySimulationIdOrderByPaymentNumberAsc(simulationId);

        return paymentScheduleMapper.toResponseList(schedule);
    }

    private void validateSimulationRequest(SimulationRequest request) {
        // Validar que la cuota inicial es suficiente (al menos 10%)
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