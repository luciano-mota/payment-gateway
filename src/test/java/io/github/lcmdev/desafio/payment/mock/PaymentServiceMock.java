package io.github.lcmdev.desafio.payment.mock;

import static io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum.PAID;
import static io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum.PENDING;

import io.github.lcmdev.desafio.payment.model.Account;
import io.github.lcmdev.desafio.payment.model.Charge;
import io.github.lcmdev.desafio.payment.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public final class PaymentServiceMock {

  public static Optional<User> originUserMock() {
    return Optional.of(User.builder()
        .id(1L)
        .name("Test")
        .cpf("12345678901")
        .email("test@test.com")
        .account(new Account(1L, BigDecimal.valueOf(5000.00), null))
        .passwordHash("test")
        .createdAt(LocalDateTime.now())
        .build());
  }

  public static Optional<User> destinationUserMock() {
    return Optional.of(User.builder()
        .id(2L)
        .name("Test")
        .cpf("02345678999")
        .email("test@test.com")
        .account(new Account(1L, BigDecimal.valueOf(10000.00), null))
        .passwordHash("test")
        .createdAt(LocalDateTime.now())
        .build());
  }

  public static Optional<User> destinationUserInsufficientBalanceMock() {
    return Optional.of(User.builder()
        .id(2L)
        .name("Test")
        .cpf("02345678999")
        .email("test@test.com")
        .account(new Account(1L, BigDecimal.valueOf(00.00), null))
        .passwordHash("test")
        .createdAt(LocalDateTime.now())
        .build());
  }

  public static Charge createChargePendingMock() {
    return Charge.builder()
        .id(1L)
        .origin(originUserMock().get())
        .destination(destinationUserMock().get())
        .amount(BigDecimal.valueOf(100.00))
        .description("Creating charge")
        .paymentMethod(null)
        .createdAt(Instant.now())
        .status(PENDING)
        .build();
  }

  public static Charge createChargeWithInsufficientBalanceMock() {
    return Charge.builder()
        .id(1L)
        .origin(originUserMock().get())
        .destination(destinationUserInsufficientBalanceMock().get())
        .amount(BigDecimal.valueOf(100.00))
        .description("Creating charge")
        .paymentMethod(null)
        .createdAt(Instant.now())
        .status(PENDING)
        .build();
  }

  public static Charge createChargePaidMock() {
    return Charge.builder()
        .id(1L)
        .origin(originUserMock().get())
        .destination(destinationUserMock().get())
        .amount(BigDecimal.valueOf(100.00))
        .description("Creating charge")
        .paymentMethod(null)
        .createdAt(Instant.now())
        .status(PAID)
        .build();
  }
}