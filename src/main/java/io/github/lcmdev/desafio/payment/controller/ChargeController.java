package io.github.lcmdev.desafio.payment.controller;

import io.github.lcmdev.desafio.payment.controller.dto.request.CardPaymentRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.CreateChargeRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.DepositRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.response.ChargeResponseDTO;
import io.github.lcmdev.desafio.payment.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.service.PaymentService;
import io.github.lcmdev.desafio.payment.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
public class ChargeController {

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<?> createCharge(@RequestBody @Valid CreateChargeRequestDTO requestDTO) {
    var userId = SecurityUtil.getCurrentUserId();
    var charge = paymentService.createCharge(userId, requestDTO.destinationCpf(),
        requestDTO.amount(), requestDTO.description());

    return ResponseEntity.created(null).body(Map.of("id", charge.getId()));
  }

  @GetMapping("/sent")
  public ResponseEntity<List<ChargeResponseDTO>> sent(
      @RequestParam(required = false) String status) {
    var userId = SecurityUtil.getCurrentUserId();
    var chargeStatus = Optional.ofNullable(status).map(String::toUpperCase)
        .map(ChargeStatusEnum::valueOf);
    var charges = paymentService.listChargesSent(userId, chargeStatus);

    if (charges.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(paymentService.listChargesSent(userId, chargeStatus));
  }

  @GetMapping("/received")
  public ResponseEntity<List<ChargeResponseDTO>> received(
      @RequestParam(required = false) String status) {
    var userId = SecurityUtil.getCurrentUserId();
    var chargeStatus = Optional.ofNullable(status).map(String::toUpperCase)
        .map(ChargeStatusEnum::valueOf);
    var chargesReceivedList = paymentService.listChargesReceived(userId, chargeStatus);

    if (chargesReceivedList.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(paymentService.listChargesReceived(userId, chargeStatus));
  }

  @PostMapping("/{id}/pay/balance")
  public ResponseEntity<?> payByBalance(@PathVariable Long id) {
    var userId = SecurityUtil.getCurrentUserId();
    var billingPayment = paymentService.payByBalance(userId, id);

    return ResponseEntity.ok(Map.of("status", billingPayment.getStatus()));
  }

  @PostMapping("/{id}/pay/card")
  public ResponseEntity<?> payByCard(@PathVariable Long id,
      @RequestBody @Valid CardPaymentRequestDTO requestDTO) {
    var userId = SecurityUtil.getCurrentUserId();
    var pay = paymentService.payByCard(
        userId,
        id,
        requestDTO.cardNumber(),
        requestDTO.expiry(),
        requestDTO.cvv()
    );
    return pay ? ResponseEntity.ok(Map.of("paid", true))
        : ResponseEntity.status(402).body(Map.of("paid", false));
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<?> cancel(@PathVariable Long id) {
    var userId = SecurityUtil.getCurrentUserId();
    var charge = paymentService.cancelCharge(userId, id);

    return ResponseEntity.ok(Map.of("status", charge.getStatus()));
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> deposit(@RequestBody @Valid DepositRequestDTO requestDTO) {
    var userId = SecurityUtil.getCurrentUserId();
    var deposited = paymentService.deposit(userId, requestDTO.amount());
    return deposited ? ResponseEntity.ok(Map.of("deposited", true))
        : ResponseEntity.status(402).body(Map.of("deposited", false));
  }
}