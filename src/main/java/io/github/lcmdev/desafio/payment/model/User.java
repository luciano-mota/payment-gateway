package io.github.lcmdev.desafio.payment.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String cpf;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Column(name = "password_hash")
    private String passwordHash;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JsonManagedReference
    private Account account;
}
