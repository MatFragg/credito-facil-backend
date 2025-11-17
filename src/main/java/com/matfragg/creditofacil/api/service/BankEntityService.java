package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.BankEntityRequest;
import com.matfragg.creditofacil.api.dto.response.BankEntityResponse;

import java.util.List;

public interface BankEntityService {

    List<BankEntityResponse> findAll();

    BankEntityResponse findById(Long id);

    BankEntityResponse update(Long id, BankEntityRequest request);

    BankEntityResponse findByName(String name);

    List<BankEntityResponse> compare(List<Long> bankIds); // Comparar múltiples bancos

    void initializeBanks(); // Pre-poblar los 18 bancos peruanos
}
