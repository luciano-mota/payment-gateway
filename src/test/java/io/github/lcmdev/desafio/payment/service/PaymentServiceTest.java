package io.github.lcmdev.desafio.payment.service;

import static io.github.lcmdev.desafio.payment.enums.ChargeStatusEnum.PENDING;
import static io.github.lcmdev.desafio.payment.enums.PaymentMethodEnum.BALANCE;
import static io.github.lcmdev.desafio.payment.enums.PaymentMethodEnum.CARD;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.createChargePaidMock;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.createChargePendingMock;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.createChargeWithInsufficientBalanceMock;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.destinationUserMock;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.originUserMock;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lcmdev.desafio.payment.client.AuthorizerClient;
import io.github.lcmdev.desafio.payment.model.User;
import io.github.lcmdev.desafio.payment.repository.ChargeRepository;
import io.github.lcmdev.desafio.payment.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChargeRepository chargeRepository;

  @Mock
  private AuthorizerClient authorizerClient;

  @Test
  void shouldCreateChargeWithSuccessWhenDadaIsOk() {
    var originId = 1L;
    var destinationCpf = "02345678999";
    var amount = 100.00;
    var description = "Test charge";

    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(userRepository.findByCpf(any())).thenReturn(destinationUserMock());
    when(chargeRepository.save(any())).thenReturn(createChargePendingMock());

    var charge = paymentService.createCharge(originId, destinationCpf, BigDecimal.valueOf(amount),
        description);

    assertAll(() -> assertNotNull(charge), () -> verify(userRepository).findById(any()),
        () -> verify(userRepository).findByCpf(any()), () -> verify(chargeRepository).save(any()));
  }

  @Test
  void shouldReturnExceptionWhenAmountIsEqualsZero() {
    var originId = 1L;
    var destinationCpf = "02345678999";
    var description = "Test charge";

    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.createCharge(originId, destinationCpf, BigDecimal.ZERO, description));

    assertAll(() -> assertNotNull(exception),
        () -> assertEquals("Value must be positive", exception.getMessage())
    );
  }

  @Test
  void shouldReturnExceptionWhenDestinationUserIsEqualsOriginUser() {
    var originId = 1L;
    var destinationCpf = "12345678901";
    var amount = 100.00;
    var description = "Test charge";

    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(userRepository.findByCpf(any())).thenReturn(Optional.of(User.builder().id(1L).build()));

    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.createCharge(originId, destinationCpf, BigDecimal.valueOf(amount),
            description));

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Origin and destination cannot be the same", exception.getMessage()));
  }

  @Test
  void shouldReturnListChargesSentWithSuccessWhenStatusIsOk() {
    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(chargeRepository.findByOriginAndStatus(any(), any())).thenReturn(
        List.of(createChargePendingMock()));

    var charges = paymentService.listChargesSent(1L, Optional.of(PENDING));

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(1, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByOriginAndStatus(any(), any())
    );
  }

  @Test
  void shouldReturnListChargesSentWithSuccessWhenStatusIsEmpty() {
    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(chargeRepository.findByOrigin(any())).thenReturn(List.of(createChargePendingMock()));

    var charges = paymentService.listChargesSent(1L, Optional.empty());

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(1, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByOrigin(any())
    );
  }

  @Test
  void shouldReturnListChargesSentEmptyWhenDoesNotExists() {
    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(chargeRepository.findByOrigin(any())).thenReturn(List.of());

    var charges = paymentService.listChargesSent(1L, Optional.empty());

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(0, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByOrigin(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenOriginUserDoesNotExistsInListChargesSent() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.listChargesSent(1L, Optional.empty())
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Origin user not found", exception.getMessage())
    );
  }

  @Test
  void shouldReturnListChargesReceivedWithSuccessWhenStatusIsOk() {
    when(userRepository.findById(any())).thenReturn(destinationUserMock());
    when(chargeRepository.findByDestinationAndStatus(any(), any())).thenReturn(
        List.of(createChargePendingMock()));

    var charges = paymentService.listChargesReceived(1L, Optional.of(PENDING));

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(1, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByDestinationAndStatus(any(), any())
    );
  }

  @Test
  void shouldReturnListChargesReceivedWithSuccessWhenStatusIsEmpty() {
    when(userRepository.findById(any())).thenReturn(originUserMock());
    when(chargeRepository.findByDestination(any())).thenReturn(List.of(createChargePendingMock()));

    var charges = paymentService.listChargesReceived(1L, Optional.empty());

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(1, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByDestination(any())
    );
  }

  @Test
  void shouldReturnListChargesReceivedEmptyWhenDoesNotExists() {
    when(userRepository.findById(any())).thenReturn(destinationUserMock());
    when(chargeRepository.findByDestination(any())).thenReturn(List.of());

    var charges = paymentService.listChargesReceived(1L, Optional.empty());

    assertAll(
        () -> assertNotNull(charges),
        () -> assertEquals(0, charges.size()),
        () -> verify(userRepository).findById(any()),
        () -> verify(chargeRepository).findByDestination(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenOriginUserDoesNotExistsInListChargesReceived() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.listChargesReceived(1L, Optional.empty())
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Destination user not found", exception.getMessage())
    );
  }

  @Test
  void shouldPayByBalanceWithSuccessWhenDataIsOk() {
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePendingMock()));
    when(chargeRepository.save(any())).thenReturn(createChargePendingMock());

    var charge = paymentService.payByBalance(2L, 1L);

    assertAll(
        () -> assertNotNull(charge),
        () -> verify(chargeRepository).findById(any()),
        () -> verify(chargeRepository).save(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenDoesNotExistsChargeInPayByBalance() {
    when(chargeRepository.findById(any())).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByBalance(2L, 1L));

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Charge not found", exception.getMessage()),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenChargeIsDifferentStatusPendingInPayByBalance() {
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePaidMock()));

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByBalance(2L, 1L));

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Charge is not pending", exception.getMessage()),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenPayerIsEqualsDestinationInPayByBalance() {
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePendingMock()));

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByBalance(1L, 1L));

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Invalid paying user", exception.getMessage()),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenUserInsufficientBalanceInPayByBalance() {
    when(chargeRepository.findById(any())).thenReturn(
        Optional.of(createChargeWithInsufficientBalanceMock()));

    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.payByBalance(2L, 1L));

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Insufficient balance", exception.getMessage()),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldDepositWithSuccessWhenDataIsOk() {
    when(authorizerClient.authorize()).thenReturn(true);
    when(userRepository.findById(any())).thenReturn(originUserMock());

    var deposited = paymentService.deposit(1L, BigDecimal.valueOf(100.00));

    assertAll(
        () -> assertTrue(deposited),
        () -> verify(authorizerClient).authorize(),
        () -> verify(userRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenAmountIsLessThanZeroInDeposit() {
    var exception = assertThrows(IllegalArgumentException.class,
        () -> paymentService.deposit(1L, BigDecimal.valueOf(00.00))
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Value must be positive", exception.getMessage())
    );
  }

  @Test
  void shouldPayByCardWithSuccessWhenDataIsOk() {
    when(authorizerClient.authorize()).thenReturn(true);
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePendingMock()));
    when(userRepository.save(any())).thenReturn(originUserMock().get());
    when(chargeRepository.save(any())).thenReturn(createChargePendingMock());

    var paid = paymentService.payByCard(2L, 1L, "1234567890123456", "1", "124");

    assertAll(
        () -> assertTrue(paid),
        () -> verify(authorizerClient).authorize(),
        () -> verify(chargeRepository).findById(any()),
        () -> verify(userRepository).save(any()),
        () -> verify(chargeRepository).save(any())
    );
  }

  @Test
  void shouldReturnExceptionInPayByCardWhenChardDoesNotExists() {
    when(authorizerClient.authorize()).thenReturn(true);
    when(chargeRepository.findById(any())).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByCard(2L, 1L, "1234567890123456", "1", "124")
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Charge not found", exception.getMessage()),
        () -> verify(authorizerClient).authorize(),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionInPayByCardWhenOriginIsEqualsDestination() {
    when(authorizerClient.authorize()).thenReturn(true);
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePendingMock()));

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByCard(1L, 1L, "1234567890123456", "1", "124")
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Invalid paying user", exception.getMessage()),
        () -> verify(authorizerClient).authorize(),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldReturnExceptionInPayByCardWhenStatusIsDifferentPending() {
    when(authorizerClient.authorize()).thenReturn(true);
    when(chargeRepository.findById(any())).thenReturn(Optional.of(createChargePaidMock()));

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.payByCard(2L, 1L, "1234567890123456", "1", "124")
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Charge is not pending", exception.getMessage()),
        () -> verify(authorizerClient).authorize(),
        () -> verify(chargeRepository).findById(any())
    );
  }

  @Test
  void shouldCancelChargeByBalance() {
    var chargeMock = createChargePaidMock();
    chargeMock.setPaymentMethod(BALANCE);

    when(chargeRepository.findById(any())).thenReturn(Optional.of(chargeMock));
    when(chargeRepository.save(any())).thenReturn(chargeMock);
    when(userRepository.save(any())).thenReturn(originUserMock().get());

    var charge = paymentService.cancelCharge(1L, 1L);

    assertAll(
        () -> assertNotNull(charge),
        () -> verify(chargeRepository).findById(any()),
        () -> verify(chargeRepository, times(1)).save(any()),
        () -> verify(userRepository, times(2)).save(any())
    );
  }

  @Test
  void shouldCancelChargeCard() {
    var chargeMock = createChargePaidMock();
    chargeMock.setPaymentMethod(CARD);

    when(chargeRepository.findById(any())).thenReturn(Optional.of(chargeMock));
    when(chargeRepository.save(any())).thenReturn(chargeMock);
    when(userRepository.save(any())).thenReturn(originUserMock().get());
    when(authorizerClient.authorize()).thenReturn(true);

    var charge = paymentService.cancelCharge(1L, 1L);

    assertAll(
        () -> assertNotNull(charge),
        () -> verify(chargeRepository).findById(any()),
        () -> verify(chargeRepository, times(1)).save(any()),
        () -> verify(userRepository, times(1)).save(any()),
        () -> verify(authorizerClient).authorize()
    );
  }
  @Test
  void shouldReturnExceptionCancelChargeCardWhenDoesNotAuthorizer() {
    var chargeMock = createChargePaidMock();
    chargeMock.setPaymentMethod(CARD);

    when(chargeRepository.findById(any())).thenReturn(Optional.of(chargeMock));
    when(authorizerClient.authorize()).thenReturn(false);

    var exception = assertThrows(IllegalStateException.class,
        () -> paymentService.cancelCharge(1L, 1L)
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Authorizer denied chargeback", exception.getMessage()),
        () -> verify(chargeRepository).findById(any()),
        () -> verify(authorizerClient).authorize()
    );
  }
}