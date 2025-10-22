package io.github.lcmdev.desafio.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "charges")
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
    private ChargeStatus status = ChargeStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Instant createdAt = Instant.now();

    public enum ChargeStatus { PENDING, PAID, CANCELED }
    public enum PaymentMethod { BALANCE, CARD }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getOrigin() { return origin; }
    public void setOrigin(User origin) { this.origin = origin; }
    public User getDestination() { return destination; }
    public void setDestination(User destination) { this.destination = destination; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ChargeStatus getStatus() { return status; }
    public void setStatus(ChargeStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

