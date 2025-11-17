package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.response.PaymentScheduleResponse;
import com.matfragg.creditofacil.api.dto.response.SimulationReportResponse;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.PaymentScheduleMapper;
import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;
import com.matfragg.creditofacil.api.model.entities.Simulation;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.PaymentScheduleRepository;
import com.matfragg.creditofacil.api.repository.SimulationRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final SimulationRepository simulationRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final PaymentScheduleMapper paymentScheduleMapper;
    private final SecurityUtils securityUtils;
    
    @Override
    public SimulationReportResponse getSimulationReport(Long simulationId) {
        log.info("Generating report for simulation ID: {}", simulationId);
        
        Simulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con ID: " + simulationId));
        
        return buildSimulationReport(simulation);
    }
    
    @Override
    public List<SimulationReportResponse> getClientSimulationsReport(Long clientId) {
        log.info("Generating simulations report for client ID: {}", clientId);
        
        List<Simulation> simulations = simulationRepository.findByClientId(clientId);
        
        if (simulations.isEmpty()) {
            log.warn("No simulations found for client ID: {}", clientId);
        }
        
        return simulations.stream()
            .map(this::buildSimulationReport)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SimulationReportResponse> getMonthlyReport(int month, int year) {
        log.info("Generating monthly report for {}/{}", month, year);
        
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        List<Simulation> simulations = simulationRepository.findByCreatedAtBetween(startDate, endDate);
        
        log.info("Found {} simulations for {}/{}", simulations.size(), month, year);
        
        return simulations.stream()
            .map(this::buildSimulationReport)
            .collect(Collectors.toList());
    }
    
    @Override
    public SimulationReportResponse getMySimulationReport(Long simulationId) {
        log.info("Generating report for authenticated user's simulation ID: {}", simulationId);
        
        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));
        Long userId = currentUser.getId();
        
        Simulation simulation = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada con ID: " + simulationId));
        
        // Verify ownership
        if (simulation.getClient().getUser() != null && !simulation.getClient().getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access simulation {} owned by another user", userId, simulationId);
            throw new UnauthorizedException("No tienes permiso para acceder a esta simulación");
        }
        
        return buildSimulationReport(simulation);
    }
    
    private SimulationReportResponse buildSimulationReport(Simulation simulation) {
        // Get payment schedule
        List<PaymentSchedule> schedules = paymentScheduleRepository.findBySimulationId(simulation.getId());
        List<PaymentScheduleResponse> scheduleResponses = schedules.stream()
            .map(paymentScheduleMapper::toResponse)
            .collect(Collectors.toList());
        
        // Calculate down payment percentage
        BigDecimal downPaymentPct = BigDecimal.ZERO;
        if (simulation.getPropertyPrice() != null && simulation.getPropertyPrice().compareTo(BigDecimal.ZERO) > 0) {
            downPaymentPct = simulation.getDownPayment()
                .divide(simulation.getPropertyPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        return SimulationReportResponse.builder()
            .simulationId(simulation.getId())
            .createdAt(simulation.getCreatedAt())
            // Client Information
            .clientName(simulation.getClient().getFirstName() + " " + simulation.getClient().getLastName())
            .clientDni(simulation.getClient().getDni())
            .clientPhone(simulation.getClient().getPhone())
            .clientMonthlyIncome(simulation.getClient().getMonthlyIncome())
            // Property Information
            .propertyAddress(simulation.getProperty().getAddress())
            .propertyCity(simulation.getProperty().getDistrict())
            .propertyDistrict(simulation.getProperty().getDistrict())
            .propertyPrice(simulation.getPropertyPrice())
            .propertyBedrooms(simulation.getProperty().getBedrooms())
            .propertyBathrooms(simulation.getProperty().getBathrooms())
            .propertyArea(simulation.getProperty().getArea())
            // Bank Information
            .bankName(simulation.getBankEntity().getName())
            .bankCurrentRate(simulation.getBankEntity().getCurrentRate())
            // Financial Details
            .downPayment(simulation.getDownPayment())
            .downPaymentPercentage(downPaymentPct)
            .governmentBonusAmount(simulation.getGovernmentBonusAmount())
            .amountToFinance(simulation.getAmountToFinance())
            .annualRate(simulation.getAnnualRate())
            .termYears(simulation.getTermYears() != null ? simulation.getTermYears() : 0)
            .monthlyPayment(simulation.getMonthlyPayment())
            .lifeInsuranceRate(simulation.getLifeInsuranceRate())
            // Financial Indicators
            .npv(simulation.getNpv())
            .irr(simulation.getIrr())
            .tcea(simulation.getTcea())
            // Payment Schedule
            .paymentSchedule(scheduleResponses)
            // Settings
            .currency(simulation.getSettings() != null ? simulation.getSettings().getCurrency() : null)
            .interestRateType(simulation.getSettings() != null ? simulation.getSettings().getInterestRateType().toString() : null)
            .capitalization(simulation.getSettings() != null ? simulation.getSettings().getCapitalization().toString() : null)
            .gracePeriodType(simulation.getSettings() != null ? simulation.getSettings().getGracePeriodType().toString() : null)
            .graceMonths(simulation.getSettings() != null ? simulation.getSettings().getGraceMonths() : null)
            .build();
    }
}
