package io.github.lcmdev.desafio.payment.service;

import io.github.lcmdev.desafio.payment.client.AuthorizerClient;
import io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.model.Charge;
import io.github.lcmdev.desafio.payment.repository.ChargeRepository;
import io.github.lcmdev.desafio.payment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum.CANCELED;
import static io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum.PAID;
import static io.github.lcmdev.desafio.payment.controller.enums.ChargeStatusEnum.PENDING;
import static io.github.lcmdev.desafio.payment.controller.enums.PaymentMethodEnum.BALANCE;
import static io.github.lcmdev.desafio.payment.controller.enums.PaymentMethodEnum.CARD;

@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final ChargeRepository chargeRepository;
    private final AuthorizerClient authorizerClient;

    public PaymentService(UserRepository userRepository, ChargeRepository chargeRepository, AuthorizerClient authorizerClient) {
        this.userRepository = userRepository;
        this.chargeRepository = chargeRepository;
        this.authorizerClient = authorizerClient;
    }

    @Transactional
    public Charge createCharge(Long originId, String destinationCpf, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        var userOrigin = userRepository.findById(originId).orElseThrow(() -> new IllegalArgumentException("Origin user not found"));
        var userDestination = userRepository.findByCpf(destinationCpf).orElseThrow(() -> new IllegalArgumentException("Destination user not found"));

        if (userOrigin.getId().equals(userDestination.getId())) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais");
        }

        var charge = Charge.builder()
                .origin(userOrigin)
                .destination(userDestination)
                .amount(amount)
                .description(description)
                .status(PENDING)
                .paymentMethod(null)
                .build();

        return chargeRepository.save(charge);
    }

    public List<Charge> listChargesSent(Long originId, Optional<ChargeStatusEnum> status) {
        var userOrigin = userRepository.findById(originId).orElseThrow();
        return status.map(chargeStatus -> chargeRepository.findByOriginAndStatus(userOrigin, chargeStatus))
                .orElseGet(() -> chargeRepository.findByOrigin(userOrigin));
    }

    public List<Charge> listChargesReceived(Long destinationId, Optional<ChargeStatusEnum> status) {
        var userDestination = userRepository.findById(destinationId).orElseThrow();
        return status.map(chargeStatus -> chargeRepository.findByDestinationAndStatus(userDestination, chargeStatus))
                .orElseGet(() -> chargeRepository.findByDestination(userDestination));
    }

    @Transactional
    public Charge payByBalance(Long payerId, Long chargeId) {
        var charge = chargeRepository.findById(chargeId).orElseThrow(() -> new IllegalStateException("Cobrança não encontrada"));

        if (!charge.getStatus().equals(ChargeStatusEnum.PENDING)) {
            throw new IllegalStateException("Cobrança não está pendente");
        }

        var payer = charge.getDestination();
        if (!payer.getId().equals(payerId)) {
            throw new IllegalStateException("Usuário pagador inválido");
        }

        var payerAccount = payer.getAccount();
        if (payerAccount.getBalance().compareTo(charge.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        var receiver = charge.getOrigin();
        var receiverAccount = receiver.getAccount();

        payerAccount.setBalance(payerAccount.getBalance().subtract(charge.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(charge.getAmount()));

        charge.setStatus(PAID);
        charge.setPaymentMethod(BALANCE);

        return chargeRepository.save(charge);
    }

    @Transactional
    public boolean deposit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }

        if (!authorizerClient.authorize()) {
            return false;
        }

        var user = userRepository.findById(userId).orElseThrow();
        var account = user.getAccount();
        account.setBalance(account.getBalance().add(amount));
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean payByCard(Long payerId, Long chargeId, String cardNumber, String expiry, String cvv) {
        if (!authorizerClient.authorize()) {
            return false;
        }

        var charge = chargeRepository.findById(chargeId).orElseThrow();

        if (!charge.getStatus().equals(PENDING)) {
            throw new IllegalStateException("Cobrança não está pendente");
        }

        if (!charge.getDestination().getId().equals(payerId)) {
            throw new IllegalStateException("Usuário pagador inválido");
        }

        charge.setStatus(PAID);
        charge.setPaymentMethod(CARD);

        var receiver = charge.getOrigin();
        var receiverAccount = receiver.getAccount();
        receiverAccount.setBalance(receiverAccount.getBalance().add(charge.getAmount()));
        userRepository.save(receiver);

        chargeRepository.save(charge);

        return true;
    }

    @Transactional
    public Charge cancelCharge(Long userId, Long chargeId) {
        var charge = chargeRepository.findById(chargeId).orElseThrow();
        authorizeCancel(userId, charge);

        if (charge.getStatus() == CANCELED) {
            return charge;
        }

        if (charge.getStatus() == PENDING) {
            return cancelPendingCharge(charge);
        }

        return cancelPaidCharge(charge);
    }

    private void authorizeCancel(Long userId, Charge charge) {
        if (!charge.getOrigin().getId().equals(userId) && !charge.getDestination().getId().equals(userId)) {
            throw new IllegalStateException("Usuário não autorizado a cancelar");
        }
    }

    private Charge cancelPendingCharge(Charge charge) {
        charge.setStatus(CANCELED);
        return chargeRepository.save(charge);
    }

    private Charge cancelPaidCharge(Charge charge) {
        if (charge.getPaymentMethod() == BALANCE) {
            return refundBalance(charge);
        } else if (charge.getPaymentMethod() == CARD) {
            return refundCard(charge);
        }
        return chargeRepository.save(charge);
    }

    private Charge refundBalance(Charge charge) {
        var receiver = charge.getOrigin();
        var receiverAccount = receiver.getAccount();
        receiverAccount.setBalance(receiverAccount.getBalance().subtract(charge.getAmount()));

        var payer = charge.getDestination();
        var payerAccount = payer.getAccount();
        payerAccount.setBalance(payerAccount.getBalance().add(charge.getAmount()));

        userRepository.save(receiver);
        userRepository.save(payer);

        charge.setStatus(CANCELED);
        return chargeRepository.save(charge);
    }

    private Charge refundCard(Charge charge) {
        if (!authorizerClient.authorize()) {
            throw new IllegalStateException("Autorizador negou estorno");
        }

        var receiver = charge.getOrigin();
        var receiverAccount = receiver.getAccount();
        receiverAccount.setBalance(receiverAccount.getBalance().subtract(charge.getAmount()));
        userRepository.save(receiver);

        charge.setStatus(CANCELED);
        return chargeRepository.save(charge);
    }
}
