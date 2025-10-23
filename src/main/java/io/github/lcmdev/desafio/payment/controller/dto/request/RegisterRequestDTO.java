package io.github.lcmdev.desafio.payment.controller.dto.request;

import io.github.lcmdev.desafio.payment.validation.CPF;

public record RegisterRequestDTO(
        String name,
        @CPF String cpf,
        String email,
        String password
) {}