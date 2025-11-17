package com.matfragg.creditofacil.api.service.impl;

import com.matfragg.creditofacil.api.dto.request.ClientRegisterRequest;
import com.matfragg.creditofacil.api.dto.request.ForgotPasswordRequest;
import com.matfragg.creditofacil.api.dto.request.LoginRequest;
import com.matfragg.creditofacil.api.dto.request.RefreshTokenRequest;
import com.matfragg.creditofacil.api.dto.request.RegisterRequest;
import com.matfragg.creditofacil.api.dto.request.ResetPasswordRequest;
import com.matfragg.creditofacil.api.dto.response.AuthResponse;
import com.matfragg.creditofacil.api.dto.response.UserResponse;
import com.matfragg.creditofacil.api.exception.BadRequestException;
import com.matfragg.creditofacil.api.exception.ResourceNotFoundException;
import com.matfragg.creditofacil.api.exception.UnauthorizedException;
import com.matfragg.creditofacil.api.mapper.UserMapper;
import com.matfragg.creditofacil.api.model.entities.Client;
import com.matfragg.creditofacil.api.model.entities.User;
import com.matfragg.creditofacil.api.model.enums.Role;
import com.matfragg.creditofacil.api.repository.ClientRepository;
import com.matfragg.creditofacil.api.repository.UserRepository;
import com.matfragg.creditofacil.api.security.JwtTokenProvider;
import com.matfragg.creditofacil.api.security.SecurityUtils;
import com.matfragg.creditofacil.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Authentication service implementation.
 * Handles user registration, login, account verification, token refresh, and password reset.
 * Includes detailed logging for tracing operations.
 * @author Ethan Matias Aliaga Aguirre - MatFragg
 * @version 1.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final ClientRepository clientRepository;
    // TODO: Implementar EmailService para envío de correos
    // private final EmailService emailService;

    /**
     * Registers a new user in the system.
     * @param request Data of the user to register
     * @return UserResponse with the created user's data
     * @throws BadRequestException if the email is already in use
     * TODO: Send verification email after registration
     */
    @Override
    public UserResponse register(RegisterRequest request) {
        // Check for unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(" The email is already in use");
        }
        
        // Create new user using the mapper
        User newUser = userMapper.toEntity(request);
        
        // Set additional fields
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setIsActive(true); // The account will be inactive until email verification but for now it is active
        newUser.setRole(Collections.singleton(Role.USER)); // Default role
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        
        // Save user
        User savedUser = userRepository.save(newUser);
        
        // TODO: Generate verification token and send email
        // String verificationToken = jwtTokenProvider.generateToken(savedUser.getEmail());
        // emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
        
        // Convert to DTO and return
        return userMapper.toResponse(savedUser);
    }

    /**
     * Registers a new client along with the associated user account.
     * @param request Data of the client and user to register
     * @return UserResponse with the created user's data
     * @throws BadRequestException if the email, username, or DNI is already in use
     * TODO: Send verification email after registration
     */
    @Override
    @Transactional
    public UserResponse registerClient(ClientRegisterRequest request) {
        // Check for unique email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("The email is already in use");
        }
        
        // Check for unique username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("The username is already in use");
        }
        
        // Check for unique DNI
        if (clientRepository.existsByDni(request.getDni())) {
            throw new BadRequestException("A client with DNI " + request.getDni() + " already exists");
        }
        
        // 1. Create user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setIsActive(true);
        newUser.setRole(Collections.singleton(Role.USER));
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        
        User savedUser = userRepository.save(newUser);
        
        // 2. Create client profile associated with the user
        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setDni(request.getDni());
        client.setPhone(request.getPhone());
        client.setMonthlyIncome(request.getMonthlyIncome());
        client.setOccupation(request.getOccupation());
        client.setUser(savedUser); // <-- Associate user with client
        client.setCreatedAt(LocalDateTime.now());
        
        clientRepository.save(client);
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
            return new UnauthorizedException("Invalid credentials");
        });
        
        // Verify that the account is active
        if (!user.getIsActive()) {
            log.warn("Login attempt with inactive account: {}", request.getEmail());
            throw new UnauthorizedException("The account is not active. Please verify your email.");
        }
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())
        );
        
        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Convert user to DTO
        UserResponse userResponse = userMapper.toResponse(user);
        
        // Return response with token and user data
        return new AuthResponse(token, userResponse);
    }

    /**
     * Verifies a user's account using a token.
     * @param token Verification token sent by email
     * @return UserResponse with the verified user's data
     * @throws BadRequestException if the token is invalid or the account is already active
     * TODO: Implement token expiration handling 
     */
    @Override
    public UserResponse verifyAccount(String token) {
        
        // Validate and extract email from token
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired verification token");
        }
        
        String email = jwtTokenProvider.getUsernameFromToken(token);
        
        // Find user
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify that the account is not already active
        if (user.getIsActive()) {
            throw new BadRequestException("The account is already verified");
        }
        
        // Activate account
        user.setIsActive(true);
        user.setLastLogin(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        
        return userMapper.toResponse(updatedUser);
    }

    /**
     * Gets the current authenticated user's data.
     * @return UserResponse with the user's data
     * @throws UnauthorizedException if there is no authenticated user
     * TODO: Optimize to avoid duplicate user lookup
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        // Get username of authenticated user
        String username = SecurityUtils.getCurrentUsername().orElseThrow(() -> new UnauthorizedException("No authenticated user"));
        
        // Find user
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userMapper.toResponse(user);
    }

    /**
     * Refreshes the JWT token using a valid refresh token.
     * @param request Contains the refresh token
     * @return AuthResponse with the new JWT token
     * @throws UnauthorizedException if the refresh token is invalid or expired
     * TODO: Implement refresh token storage and invalidation
     */
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // Validate refresh token
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        
        // Extract email from token
        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // Find user
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify that the account is active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is not active");
        }
        
        // Generate new token
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, user.getRole().stream().map(role -> (GrantedAuthority) () -> "ROLE_" + role.name()).toList());
        
        String newToken = jwtTokenProvider.generateToken(authentication);
        
        // Convert user to DTO
        UserResponse userResponse = userMapper.toResponse(user);
        
        return new AuthResponse(newToken, userResponse);
    }

    /**
     * Sends an email with instructions to reset the password.
     * @param request Contains the user's email
     * @throws ResourceNotFoundException if the user is not found
     * TODO: Implement email sending with reset link
     */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Find user by email
        userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {return new ResourceNotFoundException("If the email exists, you will receive instructions to reset your password");});
        
        // Generate reset token (valid for a limited time)
        UUID.randomUUID().toString();
        
        // TODO: Store the token in a temporary table with expiration date
        // Or use JWT with short expiration time (15-30 minutes)
        // passwordResetTokenRepository.save(new PasswordResetToken(user, resetToken, expirationDate));
        
        // TODO: Send email with reset link
        // String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;
        // emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        
    }

    /**
     * Resets the user's password using a valid token.
     * @param request Contains the token and the new password
     * @throws BadRequestException if the passwords do not match or the token is invalid/expired
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // Validate that passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // TODO: Validate the token from the temporary table
        // PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
        //         .orElseThrow(() -> new BadRequestException("Invalid token"));
        
        // TODO: Check that it has not expired
        // if (resetToken.getExpirationDate().before(new Date())) {
        //     throw new BadRequestException("Token has expired");
        // }
        
        // For now, we use the token as email (temporary)
        // In production, the token should be associated with a user in the DB
        String email = request.getToken(); // TEMPORARY - change in production
        
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // TODO: Delete the used token from the temporary table
        // passwordResetTokenRepository.delete(resetToken);
    }
}

