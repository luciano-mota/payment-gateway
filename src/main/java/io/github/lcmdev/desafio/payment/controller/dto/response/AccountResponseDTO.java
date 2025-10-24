package io.github.lcmdev.desafio.payment.controller.dto.response;

import io.github.lcmdev.desafio.payment.model.Account;
import java.math.BigDecimal;

public record AccountResponseDTO(
    Long id,
    BigDecimal balance
) {
  public static AccountResponseDTO toAccountResponse(Account account) {
    return new AccountResponseDTO(
        account.getId(),
        account.getBalance()
    );
  }
}