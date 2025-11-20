package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.response.BankComparisonResponse;
import com.matfragg.creditofacil.api.dto.response.DashboardStatsResponse;
import com.matfragg.creditofacil.api.dto.response.MonthlyMetricsResponse;
import com.matfragg.creditofacil.api.dto.response.PropertyTrendsResponse;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.model.entities.Client;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.ClientRepository;
import com.matfragg.creditofacil.api.repository.PropertyRepository;
import com.matfragg.creditofacil.api.repository.SimulationRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    
    private final SimulationRepository simulationRepository;
    private final ClientRepository clientRepository;
    private final PropertyRepository propertyRepository;
    private final SecurityUtils securityUtils;
    
    @Override
    public DashboardStatsResponse getClientStatistics(Long clientId) {
        log.info("Getting statistics for client ID: {}", clientId);
        
        clientRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clientId));
        
        long totalSimulations = simulationRepository.countByClientId(clientId);
        long totalProperties = propertyRepository.findByClientId(clientId).size();
        

        String mostUsedBank = null;
        List<Object[]> bankStats = simulationRepository.findMostUsedBankByClientId(clientId);
        if (!bankStats.isEmpty()) {
            mostUsedBank = bankStats.get(0)[0] != null ? bankStats.get(0)[0].toString() : null;
        }

        // Most popular property type for client (enum -> string)
        String mostPopularPropertyType = null;
        List<Object[]> propStats = simulationRepository.findMostPopularPropertyTypeByClientId(clientId);
        if (!propStats.isEmpty()) {
            mostPopularPropertyType = propStats.get(0)[0] != null ? propStats.get(0)[0].toString() : null;
        }

        BigDecimal totalFinanced = simulationRepository.sumFinancedAmountByClient(clientId);
        BigDecimal avgMonthlyPayment = simulationRepository.avgMonthlyPaymentByClient(clientId);
        BigDecimal avgPropertyPrice = simulationRepository.avgPropertyPriceByClient(clientId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        long simulationsThisMonth = simulationRepository.countByCreatedAtBetween(startOfMonth, now);
        long simulationsThisYear = simulationRepository.countByCreatedAtBetween(startOfYear, now);
        
        return DashboardStatsResponse.builder()
            .totalSimulations(totalSimulations)
            .totalClients(1L) // For client view, it's always 1 (themselves)
            .totalProperties(totalProperties)
            .totalFinancedAmount(totalFinanced != null ? totalFinanced : BigDecimal.ZERO)
            .averageMonthlyPayment(avgMonthlyPayment != null ? avgMonthlyPayment : BigDecimal.ZERO)
            .averagePropertyPrice(avgPropertyPrice != null ? avgPropertyPrice : BigDecimal.ZERO)
            .averageDownPayment(BigDecimal.ZERO)
            .averageLoanYears(0)
            .mostUsedBank(mostUsedBank)
            .mostPopularPropertyType(mostPopularPropertyType)
            .simulationsThisMonth(simulationsThisMonth)
            .simulationsThisYear(simulationsThisYear)
            .build();
    }
    
    @Override
    public DashboardStatsResponse getMyStatistics() {
        log.info("Getting statistics for authenticated client");
        
        User currentUser = securityUtils.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));
        Long userId = currentUser.getId();
        Client client = clientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario autenticado"));
        
        return getClientStatistics(client.getId());
    }
    
    @Override
    public DashboardStatsResponse getGlobalStatistics() {
        log.info("Getting global platform statistics");
        
        long totalSimulations = simulationRepository.countAllSimulations();
        long totalClients = clientRepository.count();
        long totalProperties = propertyRepository.count();
        
        BigDecimal totalFinanced = simulationRepository.sumFinancedAmount();
        BigDecimal avgMonthlyPayment = simulationRepository.avgMonthlyPayment();
        BigDecimal avgPropertyPrice = simulationRepository.avgPropertyPrice();
        BigDecimal avgDownPayment = simulationRepository.avgDownPayment();
        Double avgLoanYears = simulationRepository.avgLoanYears();
        
        // Most used bank
        String mostUsedBank = null;
        List<Object[]> bankStats = simulationRepository.findMostUsedBank();
        if (!bankStats.isEmpty()) {
            mostUsedBank = (String) bankStats.get(0)[0];
        }
        
        String mostPopularPropertyType = null;
        List<Object[]> propertyTypeStats = propertyRepository.findMostPopularPropertyType();
        if (!propertyTypeStats.isEmpty()) {
            mostPopularPropertyType = (String) propertyTypeStats.get(0)[0].toString();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        long simulationsThisMonth = simulationRepository.countByCreatedAtBetween(startOfMonth, now);
        long simulationsThisYear = simulationRepository.countByCreatedAtBetween(startOfYear, now);
        
        return DashboardStatsResponse.builder()
            .totalSimulations(totalSimulations)
            .totalClients(totalClients)
            .totalProperties(totalProperties)
            .totalFinancedAmount(totalFinanced != null ? totalFinanced : BigDecimal.ZERO)
            .averageMonthlyPayment(avgMonthlyPayment != null ? avgMonthlyPayment : BigDecimal.ZERO)
            .averagePropertyPrice(avgPropertyPrice != null ? avgPropertyPrice : BigDecimal.ZERO)
            .averageDownPayment(avgDownPayment != null ? avgDownPayment : BigDecimal.ZERO)
            .averageLoanYears(avgLoanYears != null ? avgLoanYears.intValue() : 0)
            .mostUsedBank(mostUsedBank)
            .mostPopularPropertyType(mostPopularPropertyType)
            .simulationsThisMonth(simulationsThisMonth)
            .simulationsThisYear(simulationsThisYear)
            .build();
    }
    
    @Override
    public List<BankComparisonResponse> getBankComparison() {
        log.info("Getting bank comparison statistics");
        
        List<Object[]> stats = simulationRepository.getBankComparisonStats();
        List<BankComparisonResponse> comparisons = new ArrayList<>();
        
        for (Object[] stat : stats) {
            Long bankId = ((Number) stat[0]).longValue();
            String bankName = (String) stat[1];
            BigDecimal currentRate = stat[2] != null ? BigDecimal.valueOf(((Number) stat[2]).doubleValue()) : null;
            Long totalSims = ((Number) stat[3]).longValue();
            BigDecimal avgFinanced = stat[4] != null ? BigDecimal.valueOf(((Number) stat[4]).doubleValue()) : BigDecimal.ZERO;
            BigDecimal avgPayment = stat[5] != null ? BigDecimal.valueOf(((Number) stat[5]).doubleValue()) : BigDecimal.ZERO;
            BigDecimal minRate = stat[6] != null ? BigDecimal.valueOf(((Number) stat[6]).doubleValue()) : BigDecimal.ZERO;
            BigDecimal maxRate = stat[7] != null ? BigDecimal.valueOf(((Number) stat[7]).doubleValue()) : BigDecimal.ZERO;
            BigDecimal avgTCEA = stat[8] != null ? BigDecimal.valueOf(((Number) stat[8]).doubleValue()) : BigDecimal.ZERO;
            
            comparisons.add(BankComparisonResponse.builder()
                .bankId(bankId)
                .bankName(bankName)
                .currentRate(currentRate)
                .totalSimulations(totalSims)
                .averageFinancedAmount(avgFinanced)
                .averageMonthlyPayment(avgPayment)
                .minRate(minRate)
                .maxRate(maxRate)
                .averageTCEA(avgTCEA)
                .build());
        }
        
        log.info("Found {} banks with simulations", comparisons.size());
        return comparisons;
    }
    
    @Override
    public PropertyTrendsResponse getPropertyTrends() {
        log.info("Getting property market trends");
        
        long totalProperties = propertyRepository.count();
        
        // Get price statistics
        BigDecimal avgPrice = propertyRepository.avgPrice();
        BigDecimal minPrice = propertyRepository.minPrice();
        BigDecimal maxPrice = propertyRepository.maxPrice();
        
        String priceRange = (minPrice != null && maxPrice != null) 
            ? String.format("%s - %s", minPrice.setScale(2, RoundingMode.HALF_UP), maxPrice.setScale(2, RoundingMode.HALF_UP))
            : null;

        String mostPopularCity = null;
        long citiesCount = propertyRepository.countDistinctCities(); // may be 0
        List<Object[]> cityStats = propertyRepository.findMostPopularCity();
        if (!cityStats.isEmpty()) {
            mostPopularCity = cityStats.get(0)[0] != null ? cityStats.get(0)[0].toString() : null;
        }

        // Get most popular district
        String mostPopularDistrict = null;
        List<Object[]> districtStats = propertyRepository.findMostPopularDistrict();
        if (!districtStats.isEmpty()) {
            mostPopularDistrict = (String) districtStats.get(0)[0];
        }
        
        // Get averages
        Double avgBeds = propertyRepository.avgBedrooms();
        BigDecimal avgArea = propertyRepository.avgArea();
        
        return PropertyTrendsResponse.builder()
            .propertiesCount(totalProperties)
            .mostPopularCity(mostPopularCity)
            .citiesCount(citiesCount)
            .mostPopularDistrict(mostPopularDistrict)
            .priceRange(priceRange)
            .averagePrice(avgPrice != null ? avgPrice : BigDecimal.ZERO)
            .minPrice(minPrice != null ? minPrice : BigDecimal.ZERO)
            .maxPrice(maxPrice != null ? maxPrice : BigDecimal.ZERO)
            .averageBedrooms(avgBeds != null ? avgBeds.intValue() : 0)
            .averageArea(avgArea != null ? avgArea : BigDecimal.ZERO)
            .build();
    }
    
    @Override
    public MonthlyMetricsResponse getMonthlyMetrics(int month, int year) {
        log.info("Getting metrics for {}/{}", month, year);
        
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        long simulationsCount = simulationRepository.countByCreatedAtBetween(startDate, endDate);
        long newClientsCount = clientRepository.countByCreatedAtBetween(startDate, endDate);
        long newPropertiesCount = propertyRepository.countByCreatedAtBetween(startDate, endDate);
        
        // Get simulations for this month to calculate averages
        List<Object[]> monthlyStats = simulationRepository.findByCreatedAtBetween(startDate, endDate)
            .stream()
            .map(sim -> new Object[]{sim.getAmountToFinance(), sim.getPropertyPrice(), sim.getMonthlyPayment()})
            .toList();
        
        BigDecimal totalFinanced = BigDecimal.ZERO;
        BigDecimal totalPropertyPrice = BigDecimal.ZERO;
        BigDecimal totalMonthlyPayment = BigDecimal.ZERO;
        
        for (Object[] stat : monthlyStats) {
            if (stat[0] != null) totalFinanced = totalFinanced.add((BigDecimal) stat[0]);
            if (stat[1] != null) totalPropertyPrice = totalPropertyPrice.add((BigDecimal) stat[1]);
            if (stat[2] != null) totalMonthlyPayment = totalMonthlyPayment.add((BigDecimal) stat[2]);
        }
        
        BigDecimal avgPropertyPrice = simulationsCount > 0 
            ? totalPropertyPrice.divide(BigDecimal.valueOf(simulationsCount), 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
            
        BigDecimal avgMonthlyPayment = simulationsCount > 0 
            ? totalMonthlyPayment.divide(BigDecimal.valueOf(simulationsCount), 2, RoundingMode.HALF_UP) 
            : BigDecimal.ZERO;
        
        return MonthlyMetricsResponse.builder()
            .month(YearMonth.of(year, month))
            .simulationsCount(simulationsCount)
            .newClientsCount(newClientsCount)
            .newPropertiesCount(newPropertiesCount)
            .totalFinancedAmount(totalFinanced)
            .averagePropertyPrice(avgPropertyPrice)
            .averageMonthlyPayment(avgMonthlyPayment)
            .build();
    }
}
