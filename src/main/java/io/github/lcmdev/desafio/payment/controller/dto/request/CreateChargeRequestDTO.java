package io.github.lcmdev.desafio.payment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateChargeRequestDTO(
        @NotBlank String destinationCpf,
        @NotNull BigDecimal amount,
        String description
) {}
