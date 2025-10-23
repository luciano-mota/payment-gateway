package io.github.lcmdev.desafio.payment.model;

import io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.controller.enums.PaymentMethodEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_charges")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_id")
    private User origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_id")
    private User destination;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private ChargeStatusEnum status;

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}