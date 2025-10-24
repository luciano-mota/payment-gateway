package io.github.lcmdev.desafio.payment.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class AuthorizerClientTest {

  @Mock
  private WebClient mockWebClient;

  @Mock
  private WebClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

  @Mock
  private WebClient.RequestHeadersSpec<?> mockRequestHeadersSpec;

  @Mock
  private WebClient.ResponseSpec mockResponseSpec;

  private AuthorizerClient authorizerClient;

  @BeforeEach
  void setUp() {
    openMocks(this);
    authorizerClient = new AuthorizerClient("http://fake-url.com");
    ReflectionTestUtils.setField(authorizerClient, "webClient", mockWebClient);
  }

  @Test
  void shouldAuthorizeReturnsTrueForAuthorizedResponse() {
    when(mockWebClient.get()).thenReturn(
        (WebClient.RequestHeadersUriSpec) mockRequestHeadersUriSpec);
    when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("AUTHORIZED"));

    boolean result = authorizerClient.authorize();

    assertTrue(result);
  }

  @Test
  void shouldAuthorizeReturnsTrueForApprovedResponse() {
    when(mockWebClient.get()).thenReturn(
        (WebClient.RequestHeadersUriSpec) mockRequestHeadersUriSpec);
    when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("APPROVED"));

    boolean result = authorizerClient.authorize();

    assertTrue(result);
  }

  @Test
  void shouldAuthorizeReturnsFalseForNullResponse() {
    when(mockWebClient.get()).thenReturn(
        (WebClient.RequestHeadersUriSpec) mockRequestHeadersUriSpec);
    when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(null));

    boolean result = authorizerClient.authorize();

    assertFalse(result);
  }

  @Test
  void shouldAuthorizeReturnsFalseForUnexpectedResponse() {
    when(mockWebClient.get()).thenReturn(
        (WebClient.RequestHeadersUriSpec) mockRequestHeadersUriSpec);
    when(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("DECLINED"));

    boolean result = authorizerClient.authorize();

    assertFalse(result);
  }

  @Test
  void shouldAuthorizeReturnsFalseOnException() {
    when(mockWebClient.get()).thenThrow(new RuntimeException("Timeout"));

    boolean result = authorizerClient.authorize();

    assertFalse(result);
  }
}