package io.github.lcmdev.desafio.payment.controller.dto.response;

import static io.github.lcmdev.desafio.payment.controller.dto.response.UserResponseDTO.toUserResponse;

import io.github.lcmdev.desafio.payment.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.enums.PaymentMethodEnum;
import io.github.lcmdev.desafio.payment.model.Charge;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ChargeResponseDTO(
    Long id,
    UserResponseDTO origin,
    UserResponseDTO destination,
    BigDecimal amount,
    String description,
    ChargeStatusEnum status,
    PaymentMethodEnum paymentMethod,
    Instant created
) {

  public static List<ChargeResponseDTO> toChargeResponse(List<Charge> charge) {
    return charge.stream()
        .map(charges -> new ChargeResponseDTO(
                charges.getId(),
                toUserResponse(charges.getOrigin()),
                toUserResponse(charges.getDestination()),
                charges.getAmount(),
                charges.getDescription(),
                charges.getStatus(),
                charges.getPaymentMethod(),
                charges.getCreatedAt()
            )
        ).toList();
  }
}