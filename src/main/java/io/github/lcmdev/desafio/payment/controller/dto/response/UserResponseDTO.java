package io.github.lcmdev.desafio.payment.controller.dto.response;

import static io.github.lcmdev.desafio.payment.controller.dto.response.AccountResponseDTO.toAccountResponse;

import io.github.lcmdev.desafio.payment.model.User;
import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String name,
    String cpf,
    String email,
    String passwordHash,
    LocalDateTime createdAt,
    AccountResponseDTO account
) {
  public static UserResponseDTO toUserResponse(User user) {
    return new UserResponseDTO(
        user.getId(),
        user.getName(),
        user.getCpf(),
        user.getEmail(),
        user.getPasswordHash(),
        user.getCreatedAt(),
        toAccountResponse(user.getAccount())
    );
  }
}
