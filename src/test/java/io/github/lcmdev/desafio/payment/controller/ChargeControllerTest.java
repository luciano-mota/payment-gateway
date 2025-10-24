package io.github.lcmdev.desafio.payment.controller;

import static io.github.lcmdev.desafio.payment.controller.dto.response.ChargeResponseDTO.toChargeResponse;
import static io.github.lcmdev.desafio.payment.mock.PaymentServiceMock.createChargePendingMock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lcmdev.desafio.payment.controller.dto.request.CardPaymentRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.CreateChargeRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.DepositRequestDTO;
import io.github.lcmdev.desafio.payment.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.model.Charge;
import io.github.lcmdev.desafio.payment.service.PaymentService;
import io.github.lcmdev.desafio.payment.util.SecurityUtil;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ChargeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PaymentService paymentService;

  private MockedStatic<SecurityUtil> mockedSecurityUtil;

  @BeforeEach
  void setUp() {
    mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
  }

  @AfterEach
  void tearDown() {
    mockedSecurityUtil.close();
  }

  @Test
  void shouldCreateChargeWithSuccessWhenDataIsOk() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

    var requestDTO = new CreateChargeRequestDTO("02345678999", new BigDecimal("150.00"), "New Test Charge");
    var mockCharge = Mockito.mock(Charge.class);

    when(mockCharge.getId()).thenReturn(10L);
    when(paymentService.createCharge(eq(1L), eq("02345678999"), eq(new BigDecimal("150.00")),
        eq("New Test Charge")))
        .thenReturn(mockCharge);

    mockMvc.perform(post("/api/v1/charges")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()));
  }

  @Test
  void shouldReturnNoContentWhenNoSentCharges() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

    when(paymentService.listChargesSent(eq(1L), any())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/v1/charges/sent"))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnOkWhenSentChargesExists() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

    when(paymentService.listChargesSent(eq(1L), any())).thenReturn(
        toChargeResponse(List.of(createChargePendingMock())));

    mockMvc.perform(get("/api/v1/charges/sent"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnNoContentWhenNoReceivedCharges() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
    when(paymentService.listChargesReceived(eq(2L), any())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/v1/charges/received").param("status", "PENDING"))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldPayByBalanceWithSuccess() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
    var paidCharge = Mockito.mock(Charge.class);
    when(paidCharge.getStatus()).thenReturn(ChargeStatusEnum.PAID);
    when(paymentService.payByBalance(eq(2L), eq(1L))).thenReturn(paidCharge);

    mockMvc.perform(post("/api/v1/charges/1/pay/balance"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("PAID")));
  }

  @Test
  void shouldReturnInternalServerErroForInsufficientBalance() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
    when(paymentService.payByBalance(eq(2L), eq(2L)))
        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));

    mockMvc.perform(post("/api/v1/charges/2/pay/balance"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldPayByCardWithSuccess() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3L);
    when(
        paymentService.payByCard(eq(3L), eq(3L), anyString(), anyString(), anyString())).thenReturn(
        true);

    var requestDTO = new CardPaymentRequestDTO("1234567890123456", "12/25", "123");

    mockMvc.perform(post("/api/v1/charges/3/pay/card")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paid", is(true)));
  }

  @Test
  void shouldCancelBalancePaidCharge() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
    var canceledCharge = Mockito.mock(Charge.class);
    when(canceledCharge.getStatus()).thenReturn(ChargeStatusEnum.CANCELED);
    when(paymentService.cancelCharge(eq(1L), eq(4L))).thenReturn(canceledCharge);

    mockMvc.perform(post("/api/v1/charges/4/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("CANCELED")));
  }

  @Test
  void shouldDepositWithSuccess() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
    when(paymentService.deposit(eq(1L), eq(new BigDecimal("300.00")))).thenReturn(true);

    var requestDTO = new DepositRequestDTO(new BigDecimal("300.00"));

    mockMvc.perform(post("/api/v1/charges/deposit")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deposited", is(true)));
  }

  @Test
  void shouldReturnPaymentRequiredWhenCardDeclined() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3L);
    when(paymentService.payByCard(eq(3L), eq(99L), anyString(), anyString(),
        anyString())).thenReturn(false);

    var requestDTO = new CardPaymentRequestDTO("4111111111111111", "10/30", "999");

    mockMvc.perform(post("/api/v1/charges/99/pay/card")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isPaymentRequired())
        .andExpect(jsonPath("$.paid", is(false)));
  }

  @Test
  void shouldReturnPaymentRequiredWhenDepositFails() throws Exception {
    mockedSecurityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
    when(paymentService.deposit(eq(1L), eq(new BigDecimal("1.00")))).thenReturn(false);

    var requestDTO = new DepositRequestDTO(new BigDecimal("1.00"));

    mockMvc.perform(post("/api/v1/charges/deposit")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isPaymentRequired())
        .andExpect(jsonPath("$.deposited", is(false)));
  }
}