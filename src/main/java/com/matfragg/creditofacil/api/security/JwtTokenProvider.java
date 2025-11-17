package com.matfragg.creditofacil.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider.
 * Generates and validates JWT tokens.
 * Handles extraction of username and roles from tokens.
 * @version 1.0
 * @author Ethan Matias Aliaga Aguirre - MatFragg 
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    /**
     * Gets the signing key for JWT.
     * @return SecretKey used for signing JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for the given authentication.
     * @param authentication Authentication object containing user details
     * @return Generated JWT token as a String
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        SecretKey key = getSigningKey();

        // Extrae los roles del usuario
        List<String> roles = userDetails.getAuthorities().stream()
            .map(auth -> auth.getAuthority().replace("ROLE_", "")) // Opcional: quita el prefijo
            .collect(Collectors.toList());

        return Jwts.builder()
                .claim(Claims.SUBJECT, userDetails.getUsername())
                .claim(Claims.ISSUED_AT, now)
                .claim(Claims.EXPIRATION, expiryDate)
                .claim("roles", roles) // <-- Agrega los roles aquí
                .signWith(key)
                .compact();
    }

    /**
     * Extracts username from the JWT token.
     * @param token JWT token as a String
     * @return Username extracted from the token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates the JWT token.
     * @param authToken JWT token as a String
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts roles from the JWT token.
     * @param token JWT token as a String
     * @return List of roles extracted from the token
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            List<?> rolesList = (List<?>) rolesObj;
            List<String> roles = new ArrayList<>();
            for (Object role : rolesList) {
                roles.add(role.toString());
            }
            return roles;
        }
        return new ArrayList<>();
    }
}