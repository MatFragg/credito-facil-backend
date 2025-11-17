package com.matfragg.creditofacil.api.service;

import com.matfragg.creditofacil.api.dto.request.ChangePasswordRequest;
import com.matfragg.creditofacil.api.dto.request.UserProfileRequest;
import com.matfragg.creditofacil.api.dto.response.UserResponse;

import io.micrometer.common.lang.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User service interface.
 * Handles operations related to user management including profile retrieval, updates, password changes, and admin functionalities.
 * 
 * @version 1.0
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 */
public interface UserService {
    
    /**
     * Obtains the profile of the currently authenticated user.
     * 
     * @return UserResponse containing the user's profile information.
     */
    UserResponse getProfile();
    
    /**
     * Updates the profile of the currently authenticated user.
     * 
     * @param request UserProfileRequest containing the new profile information.
     * @return UserResponse containing the updated user's profile information.
     */
    UserResponse updateProfile(UserProfileRequest request);
    
    /**
     * Changes the password of the currently authenticated user.
     * 
     * @param request ChangePasswordRequest containing the current and new passwords.
     */
    void changePassword(ChangePasswordRequest request);
    
    /**
     * Deletes a user by their ID (admin only).
     * 
     * @param id ID of the user to delete.
     */
    void deleteUser(Long id);
    
    /**
     * Lists all users with pagination (admin only).
     * 
     * @param pageable Pagination configuration.
     * @return Page containing the found users.
     */
    Page<UserResponse> listUsers(@NonNull Pageable pageable);
    
    /**
     * Finds a user by their ID (admin only).
     * 
     * @param id ID of the user to find.
     * @return UserResponse containing the user's data.
     */
    UserResponse findById(Long id);
    
    /**
     * Finds a user by their email (admin only).
     * 
     * @param email Email of the user to find.
     * @return UserResponse containing the user's data.
     */
    UserResponse findByEmail(String email);
    
    /**
     * Activates or deactivates a user (admin only).
     * 
     * @param id ID of the user to modify.
     * @param isActive Status to set (active or inactive).
     * @return UserResponse containing the updated data.
     */
    UserResponse toggleUserStatus(Long id, boolean isActive);
}
