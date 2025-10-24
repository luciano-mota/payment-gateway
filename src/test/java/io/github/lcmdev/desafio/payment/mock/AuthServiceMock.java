package io.github.lcmdev.desafio.payment.mock;

import io.github.lcmdev.desafio.payment.model.Account;
import io.github.lcmdev.desafio.payment.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public final class AuthServiceMock {

  public static Optional<User> createUserMock() {
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
}