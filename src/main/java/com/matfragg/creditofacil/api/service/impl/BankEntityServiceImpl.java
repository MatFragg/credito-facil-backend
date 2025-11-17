package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.BankEntityRequest;
import com.matfragg.creditofacil.api.dto.response.BankEntityResponse;
import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.mapper.BankEntityMapper;
import com.matfragg.creditofacil.api.model.entities.BankEntity;
import com.matfragg.creditofacil.api.repository.BankEntityRepository;
import com.matfragg.creditofacil.api.service.BankEntityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank Entity service implementation.
 * Handles operations related to bank entities including retrieval, updating, comparison, and initialization.
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BankEntityServiceImpl implements BankEntityService {

    private final BankEntityRepository bankEntityRepository;
    private final BankEntityMapper bankEntityMapper;

    @PostConstruct
    public void init() {
        // Inicializar bancos al arrancar la aplicación si no existen
        if (bankEntityRepository.count() == 0) {
            initializeBanks();
        }
    }

    /**
     * Retrieves all bank entities.
     * @return List of BankEntityResponse containing all bank entities
     * @throws ResourceNotFoundException if no bank entities are found
     */
    @Override
    @Transactional(readOnly = true)
    public List<BankEntityResponse> findAll() {
        List<BankEntity> banks = bankEntityRepository.findAll();
        return bankEntityMapper.toResponseList(banks);
    }

    /**
     * Retrieves a bank entity by its ID.
     * @param id ID of the bank entity
     * @return BankEntityResponse containing the bank entity data
     * @throws ResourceNotFoundException if the bank entity is not found
     */
    @Override
    @Transactional(readOnly = true)
    public BankEntityResponse findById(Long id) {
        if (id == null || id <= 0) throw new BadRequestException("Invalid bank entity ID");
        var bank = bankEntityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bank entity not found with id: " + id));
        return bankEntityMapper.toResponse(bank);
    }

    /**
     * Updates a bank entity by its ID.
     * @param id ID of the bank entity to update
     * @param request BankEntityRequest containing updated data
     * @return BankEntityResponse containing the updated bank entity data
     * @throws ResourceNotFoundException if the bank entity is not found
     */
    @Override
    public BankEntityResponse update(Long id, BankEntityRequest request) {
        if (id == null || id <= 0) throw new BadRequestException("Invalid bank entity ID");

        var bank = bankEntityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bank entity not found with id: " + id));

        // Update fields
        bankEntityMapper.updateEntityFromRequest(request, bank);
        bank.setLastUpdated(LocalDate.now());

        var updated = bankEntityRepository.save(bank);
        
        return bankEntityMapper.toResponse(updated);
    }

    /**
     * Finds a bank entity by its name.
     * @param name Name of the bank entity
     * @return BankEntityResponse containing the bank entity data
     * @throws ResourceNotFoundException if the bank entity is not found
     */
    @Override
    @Transactional(readOnly = true)
    public BankEntityResponse findByName(String name) {
        var bank = bankEntityRepository.findByNameContainingIgnoreCase(name).orElseThrow(() -> new ResourceNotFoundException("Bank Entity not found with name: " + name));
        return bankEntityMapper.toResponse(bank);
    }

    /**
     * Compares multiple bank entities by their IDs.
     * @param bankIds List of bank entity IDs to compare
     * @return List of BankEntityResponse containing the compared bank entities
     * @throws BadRequestException if no IDs are provided
     * @throws ResourceNotFoundException if any of the bank entities are not found
     */
    @Override
    @Transactional(readOnly = true)
    public List<BankEntityResponse> compare(List<Long> bankIds) {
        
        if (bankIds == null || bankIds.isEmpty()) {
            throw new BadRequestException("At least one bank ID must be provided for comparison");
        }

        List<BankEntity> banks = bankEntityRepository.findAllById(bankIds);
        
        if (banks.size() != bankIds.size()) {
            throw new ResourceNotFoundException("Some bank IDs were not found");
        }

        return bankEntityMapper.toResponseList(banks);
    }

    /**
     * Initializes the database with a predefined list of bank entities.
     * This method is called at application startup if no bank entities exist.
     */
    @Override
    public void initializeBanks() {

        List<BankEntity> banks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        banks.add(createBankEntity("Banco de Crédito del Perú (BCP)", 
            new BigDecimal("7.50"), new BigDecimal("3000.00"), new BigDecimal("90.00"), today));

        banks.add(createBankEntity("BBVA Perú", 
            new BigDecimal("7.80"), new BigDecimal("3200.00"), new BigDecimal("85.00"), today));

        banks.add(createBankEntity("Scotiabank Perú", 
            new BigDecimal("7.60"), new BigDecimal("3100.00"), new BigDecimal("90.00"), today));

        banks.add(createBankEntity("Interbank", 
            new BigDecimal("7.90"), new BigDecimal("3000.00"), new BigDecimal("85.00"), today));
        
            banks.add(createBankEntity("Banco Pichincha", 
            new BigDecimal("8.20"), new BigDecimal("2800.00"), new BigDecimal("80.00"), today));

        banks.add(createBankEntity("Mibanco", 
            new BigDecimal("9.50"), new BigDecimal("2000.00"), new BigDecimal("75.00"), today));

        banks.add(createBankEntity("Banco GNB", 
            new BigDecimal("8.50"), new BigDecimal("2900.00"), new BigDecimal("80.00"), today));

        banks.add(createBankEntity("Banco Falabella", 
            new BigDecimal("8.80"), new BigDecimal("2500.00"), new BigDecimal("75.00"), today));

        banks.add(createBankEntity("Banco Ripley", 
            new BigDecimal("9.00"), new BigDecimal("2400.00"), new BigDecimal("70.00"), today));

        banks.add(createBankEntity("Banco Santander Perú", 
            new BigDecimal("8.00"), new BigDecimal("3300.00"), new BigDecimal("85.00"), today));

        banks.add(createBankEntity("Citibank Perú", 
            new BigDecimal("7.70"), new BigDecimal("3500.00"), new BigDecimal("90.00"), today));

        banks.add(createBankEntity("ICBC Peru Bank", 
            new BigDecimal("8.30"), new BigDecimal("3000.00"), new BigDecimal("80.00"), today));

        banks.add(createBankEntity("Banco Interamericano de Finanzas (BanBif)", 
            new BigDecimal("8.40"), new BigDecimal("2900.00"), new BigDecimal("85.00"), today));

        banks.add(createBankEntity("Bank of China", 
            new BigDecimal("8.10"), new BigDecimal("3200.00"), new BigDecimal("80.00"), today));

        banks.add(createBankEntity("Alfin Banco", 
            new BigDecimal("9.20"), new BigDecimal("2300.00"), new BigDecimal("70.00"), today));

        banks.add(createBankEntity("Banco de Comercio", 
            new BigDecimal("8.70"), new BigDecimal("2600.00"), new BigDecimal("75.00"), today));

        banks.add(createBankEntity("Caja Metropolitana", 
            new BigDecimal("9.80"), new BigDecimal("2000.00"), new BigDecimal("70.00"), today));

        banks.add(createBankEntity("Caja Arequipa", 
            new BigDecimal("9.60"), new BigDecimal("2100.00"), new BigDecimal("75.00"), today));

        bankEntityRepository.saveAll(banks);
    }

    /**
     * Helper method to create a BankEntity instance.
     * @param name The name of the bank.
     * @param currentRate The current interest rate offered by the bank.
     * @param minimumIncome The minimum income required to apply for a loan.
     * @param maxCoveragePct The maximum coverage percentage for loans.
     * @param lastUpdated The date when the bank data was last updated.
     * @return A new BankEntity instance with the provided data.
     */
    private BankEntity createBankEntity(String name, BigDecimal currentRate, BigDecimal minimumIncome, BigDecimal maxCoveragePct, LocalDate lastUpdated) {
        return BankEntity.builder()
        .name(name)
        .currentRate(currentRate)
        .minimumIncome(minimumIncome)
        .maxCoveragePct(maxCoveragePct)
        .lastUpdated(lastUpdated)
        .build();
    }
}
