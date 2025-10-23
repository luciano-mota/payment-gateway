package io.github.lcmdev.desafio.payment.controller;

import io.github.lcmdev.desafio.payment.controller.dto.request.CardPaymentRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.CreateChargeRequestDTO;
import io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.model.Charge;
import io.github.lcmdev.desafio.payment.service.PaymentService;
import io.github.lcmdev.desafio.payment.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createCharge(@RequestBody @Valid CreateChargeRequestDTO requestDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        Charge c = paymentService.createCharge(userId, requestDTO.destinationCpf(), requestDTO.amount(), requestDTO.description());
        return ResponseEntity.ok(Map.of("id", c.getId()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Charge>> sent(@RequestParam(required = false) String status) {
        Long userId = SecurityUtil.getCurrentUserId();
        var chargeStatus = Optional.ofNullable(status).map(String::toUpperCase).map(ChargeStatusEnum::valueOf);
        var charges = paymentService.listChargesSent(userId, chargeStatus);

        if (charges.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(paymentService.listChargesSent(userId, chargeStatus));
    }

    @GetMapping("/received")
    public ResponseEntity<List<Charge>> received(@RequestParam(required = false) String status) {
        Long userId = SecurityUtil.getCurrentUserId();
        var chargeStatus = Optional.ofNullable(status).map(String::toUpperCase).map(ChargeStatusEnum::valueOf);
        var chargesReceivedList = paymentService.listChargesReceived(userId, chargeStatus);

        if (chargesReceivedList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(paymentService.listChargesReceived(userId, chargeStatus));
    }

    @PostMapping("/{id}/pay/balance")
    public ResponseEntity<?> payByBalance(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        var billingPayment = paymentService.payByBalance(userId, id);
        return ResponseEntity.ok(Map.of("status", billingPayment.getStatus()));
    }

    @PostMapping("/{id}/pay/card")
    public ResponseEntity<?> payByCard(@PathVariable Long id, @RequestBody @Valid CardPaymentRequestDTO requestDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        var pay = paymentService.payByCard(
                userId,
                id,
                requestDTO.cardNumber(),
                requestDTO.expiry(),
                requestDTO.cvv()
        );
        return pay ? ResponseEntity.ok(Map.of("paid", true)) : ResponseEntity.status(402).body(Map.of("paid", false));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Charge c = paymentService.cancelCharge(userId, id);
        return ResponseEntity.ok(Map.of("status", c.getStatus()));
    }

    public record DepositRequest(@NotNull BigDecimal amount) {}

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody @Valid DepositRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        var deposited = paymentService.deposit(userId, req.amount());
        return deposited ? ResponseEntity.ok(Map.of("deposited", true)) : ResponseEntity.status(402).body(Map.of("deposited", false));
    }
}