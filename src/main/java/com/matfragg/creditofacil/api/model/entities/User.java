package com.matfragg.creditofacil.api.model.entities;

import com.matfragg.creditofacil.api.model.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name="first_name", nullable=false, length=50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name="last_name", nullable=false, length=50)
    private String lastName;


    @NotBlank
    @Column(name="username", unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false, length=50)
    private String password;

    @Column(unique = true, nullable = false)
    private Boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Roles> roles = new HashSet<>();

    @LastModifiedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
