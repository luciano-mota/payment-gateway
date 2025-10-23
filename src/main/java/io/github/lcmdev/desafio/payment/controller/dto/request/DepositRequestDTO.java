package io.github.lcmdev.desafio.payment.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequestDTO(@NotNull BigDecimal amount) {}