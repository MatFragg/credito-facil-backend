package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.ClientRequest;
import com.matfragg.creditofacil.api.dto.response.ClientResponse;
import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.ClientMapper;
import com.matfragg.creditofacil.api.model.entities.Client;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.ClientRepository;
import com.matfragg.creditofacil.api.repository.UserRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.ClientService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;

    /**
     * Lists all clients with pagination.
     * @param pageable Pageable object containing pagination information.
     * @return Page of ClientResponse containing the paginated list of clients.
     * @throws ResourceNotFoundException if no clients are found.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(@NonNull Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable cannot be null");

        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(clientMapper::toResponse);
    }

    /**
     * Finds a client by their ID.
     * 
     * @param id Long representing the client's ID.
     * @return ClientResponse containing the client's information.
     * @throws ResourceNotFoundException if the client is not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        
        if (id == null) throw new IllegalArgumentException("Client id cannot be null");

        var client = clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));
        
        return clientMapper.toResponse(client);
    }

    /**
     * Creates a new client.
     */
    @Override
    public ClientResponse create(ClientRequest request) {

        if (request.getDni() != null && clientRepository.existsByDni(request.getDni())) {
            throw new BadRequestException("A client with DNI already exists: " + request.getDni());
        }

        var client = clientMapper.toEntity(request);
        client.setCreatedAt(LocalDateTime.now());

        // Associate profile to authenticated user (normal flow)
        String currentEmail = SecurityUtils.getCurrentUsername().orElse(null);
        if (currentEmail != null) {
            userRepository.findByEmail(currentEmail).ifPresentOrElse(user -> {client.setUser(user);
            }, () -> {
                throw new UnauthorizedException("Authenticated user not found in database");
            });
        } else {
            throw new UnauthorizedException("No authenticated user found");
        }

        var savedClient = clientRepository.save(client);

        return clientMapper.toResponse(savedClient);
    }
    
    /**
     * Updates an existing client.
     * 
     * @param id Long representing the client's ID.
     * @param request ClientRequest containing updated data.
     * @return ClientResponse containing the updated client's information.
     * @throws ResourceNotFoundException if the client is not found in the database.
     * @throws BadRequestException if the updated DNI already exists for another client.
     */
    @Override
    public ClientResponse update(Long id, ClientRequest request) {

        if (id == null) throw new IllegalArgumentException("Client id cannot be null");
        var client = clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));

        if (request.getDni() != null && !request.getDni().equals(client.getDni()) &&
            clientRepository.existsByDni(request.getDni())) {
            throw new BadRequestException("A client with DNI already exists: " + request.getDni());
        }

        // Update basic data
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setDni(request.getDni());
        client.setPhone(request.getPhone());
        client.setMonthlyIncome(request.getMonthlyIncome());
        client.setOccupation(request.getOccupation());

        Client updatedClient = clientRepository.save(client);
        log.info("Client updated successfully with ID: {}", id);

        return clientMapper.toResponse(updatedClient);
    }
    
    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Client id cannot be null");

        var client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));

        // TODO: Verify that there are no active simulations
        // long simulationCount = simulationRepository.countByClientId(id);
        // if (simulationCount > 0) {
        //     throw new BadRequestException("Cannot delete client because they have active simulations");
        // }
        
        // Eliminar cliente
        clientRepository.delete(client);
        
        // Si tiene usuario asociado, eliminarlo también
        if (client.getUser() != null) {
            userRepository.delete(client.getUser());
        }
        
        log.info("Cliente eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findByDni(String dni) {
        log.info("Buscando cliente por DNI: {}", dni);
        
        Client client = clientRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dni));
        
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getMyProfile() {
        log.info("Obteniendo perfil del cliente autenticado");
        
        String email = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new UnauthorizedException("No hay usuario autenticado"));
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para este usuario"));
        
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> findProspects(Pageable pageable) {
        log.info("Listando clientes prospectos");
        
        // Filtrar clientes sin usuario (user_id IS NULL)
        Page<Client> prospects = clientRepository.findAll(
                (root, query, cb) -> cb.isNull(root.get("user")), 
                pageable
        );
        
        return prospects.map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> findRegistered(Pageable pageable) {
        log.info("Listando clientes registrados");
        
        // Filtrar clientes con usuario (user_id IS NOT NULL)
        Page<Client> registered = clientRepository.findAll(
                (root, query, cb) -> cb.isNotNull(root.get("user")), 
                pageable
        );
        
        return registered.map(clientMapper::toResponse);
    }
}
