package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.ChangePasswordRequest;
import com.matfragg.creditofacil.api.dto.request.UserProfileRequest;
import com.matfragg.creditofacil.api.dto.response.UserResponse;
import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.UserMapper;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.repository.UserRepository;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.UserService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User service implementation.
 * Handles user profile management, password changes, user listing, and status toggling.
 * Includes detailed logging for tracing operations.
 * 
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtains the profile of the currently authenticated user.
     * 
     * @return UserResponse containing the user's profile information.
     * @throws UnauthorizedException if no user is authenticated.
     * @throws ResourceNotFoundException if the user is not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        // Gets the email of the currently authenticated user        
        String email = SecurityUtils.getCurrentUsername().orElseThrow(() -> new UnauthorizedException("There is no authenticated user"));
        
        // Finds the user entity by email
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toResponse(user);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * 
     * @param request UserProfileRequest containing the new profile information.
     * @return UserResponse containing the updated user's profile information.
     * @throws UnauthorizedException if no user is authenticated.
     * @throws ResourceNotFoundException if the user is not found in the database.
     * @throws BadRequestException if the new email is already in use by another user.
     */
    @Override
    public UserResponse updateProfile(UserProfileRequest request) {

        // Gets the email of the currently authenticated user
        String currentEmail = SecurityUtils.getCurrentUsername().orElseThrow(() -> new UnauthorizedException("There is no authenticated user"));
        
        // Finds the user entity by email
        User user = userRepository.findByEmail(currentEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validates if the new email is already in use by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("The email is already in use");
        }
        
        /**
         * Updates the user's profile information
         */
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User updatedUser = userRepository.save(user);
        
        return userMapper.toResponse(updatedUser);
    }

    /**
     * Changes the password of the currently authenticated user.
     * 
     * @param request ChangePasswordRequest containing the current and new passwords.
     * @throws UnauthorizedException if no user is authenticated.
     * @throws ResourceNotFoundException if the user is not found in the database.
     * @throws BadRequestException if the current password is incorrect, if the new passwords do not match, or if the new password is the same as the current one.
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        // Gets the email of the currently authenticated user
        String email = SecurityUtils.getCurrentUsername().orElseThrow(() -> new UnauthorizedException("There is no authenticated user"));
        
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validates the current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("The current password is incorrect");
        }
        
        // Validates that the new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("The passwords do not match");
        }

        // Validates that the new password is different from the current one
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("The new password must be different from the current one");
        }
        
        // Updates the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param id Long representing the user's ID.
     * @throws ResourceNotFoundException if the user is not found in the database.
     * @throws BadRequestException if the user attempts to delete their own account.
     */
    @Override
    public void deleteUser(Long id) {
        if (id == null || id <= 0) throw new BadRequestException("Invalid user ID");

        // Find the user by ID
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
                
        // Prevent a user from deleting themselves
        String currentEmail = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user"));

        // Prevent a user from deleting themselves
        if (user.getEmail().equals(currentEmail)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    /**
     * Lists users with pagination.
     * 
     * @param pageable Pageable object containing pagination information.
     * @return Page of UserResponse containing the paginated list of users.
     * @throws ResourceNotFoundException if no users are found.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(@NonNull Pageable pageable) {
        Objects.requireNonNull(pageable, "Pageable cannot be null");

        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    /**
     * Finds a user by their ID.
     * 
     * @param id Long representing the user's ID.
     * @return UserResponse containing the user's information.
     * @throws ResourceNotFoundException if the user is not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        if (id == null || id <= 0) throw new BadRequestException("Invalid user ID");

        var user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponse(user);
    }

    /**
     * Finds a user by their email.
     * 
     * @param email String representing the user's email.
     * @return UserResponse containing the user's information.
     * @throws ResourceNotFoundException if the user is not found in the database.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        
        var user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return userMapper.toResponse(user);
    }

    /**
     * Toggles the active status of a user.
     * 
     * @param id Long representing the user's ID.
     * @param isActive boolean indicating the desired active status.
     * @return UserResponse containing the updated user's information.
     * @throws ResourceNotFoundException if the user is not found in the database.
     * @throws BadRequestException if the user attempts to deactivate their own account.
     */
    @Override
    public UserResponse toggleUserStatus(Long id, boolean isActive) {
        if (id == null || id <= 0) throw new BadRequestException("Invalid user ID");

        // Find the user by ID
        var user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Prevent a user from deactivating themselves
        String currentEmail = SecurityUtils.getCurrentUsername().orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        // Prevent a user from deactivating themselves
        if (user.getEmail().equals(currentEmail) && !isActive) {
            throw new BadRequestException("You cannot deactivate your own account");
        }
        
        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);
        
        return userMapper.toResponse(updatedUser);
    }
}
