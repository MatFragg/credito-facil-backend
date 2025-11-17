package com.matfragg.creditofacil.api.dto.response;

import com.matfragg.creditofacil.api.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
