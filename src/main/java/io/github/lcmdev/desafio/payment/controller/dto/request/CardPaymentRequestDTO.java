package io.github.lcmdev.desafio.payment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CardPaymentRequestDTO(
        @NotBlank String cardNumber,
        @NotBlank String expiry,
        @NotBlank String cvv
) {}