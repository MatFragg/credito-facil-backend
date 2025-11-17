package com.matfragg.creditofacil.api.repository;

import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.model.enums.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User repository interface for CRUD operations on User entities.
 * Handles data access related to users including finding by email, username, and role-based counts.
 * @version 1.0
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByRole(Role role);
}