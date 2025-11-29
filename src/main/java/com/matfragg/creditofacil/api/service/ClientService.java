package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.ClientRequest;
import com.matfragg.creditofacil.api.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de gestión de clientes
 * Maneja operaciones CRUD y gestión de clientes prospectos y registrados
 */
public interface ClientService {
    
    /**
     * Lista todos los clientes con paginación
     * 
     * @param pageable Configuración de paginación
     * @return Página con los clientes encontrados
     */
    Page<ClientResponse> findAll(Pageable pageable);
    
    /**
     * Busca un cliente por su ID
     * 
     * @param id ID del cliente
     * @return ClientResponse con los datos del cliente
     */
    ClientResponse findById(Long id);
    
    /**
     * Crea un nuevo cliente (prospecto o registrado)
     * 
     * @param request Datos del cliente a crear
     * @return ClientResponse con los datos del cliente creado
     */
    ClientResponse create(ClientRequest request);
    
    /**
     * Actualiza los datos de un cliente existente
     * 
     * @param id ID del cliente a actualizar
     * @param request Datos actualizados
     * @return ClientResponse con los datos actualizados
     */
    ClientResponse update(Long id, ClientRequest request);
    
    /**
     * Elimina un cliente por su ID
     * 
     * @param id ID del cliente a eliminar
     */
    void delete(Long id);
    
    /**
     * Busca un cliente por su DNI
     * 
     * @param dni DNI del cliente
     * @return ClientResponse con los datos del cliente
     */
    ClientResponse findByDni(String dni);
    
    /**
     * Obtiene el perfil del cliente autenticado
     * 
     * @return ClientResponse con los datos del cliente
     */
    Page<ClientResponse> getMyClients(Pageable pageable);
    
    /**
     * Lista los clientes prospectos (sin cuenta de usuario)
     * 
     * @param pageable Configuración de paginación
     * @return Página con los clientes prospectos
     */
    Page<ClientResponse> findProspects(Pageable pageable);
    
    /**
     * Lista los clientes registrados (con cuenta de usuario)
     * 
     * @param pageable Configuración de paginación
     * @return Página con los clientes registrados
     */
    Page<ClientResponse> findRegistered(Pageable pageable);
}
