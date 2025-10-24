package io.github.lcmdev.desafio.payment.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest {

  @AfterEach
  void tearDown() {
    clearContext();
  }

  @Test
  void shouldReturnNullWhenAuthenticationIsNull() {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(context);

    Long result = SecurityUtil.getCurrentUserId();
    assertNull(result);
  }

  @Test
  void shouldReturnUserIdWhenPrincipalIsLong() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(42L);

    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Long result = SecurityUtil.getCurrentUserId();
    assertEquals(42L, result);
  }

  @Test
  void shouldParseUserIdWhenPrincipalIsString() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("123");

    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Long result = SecurityUtil.getCurrentUserId();
    assertEquals(123L, result);
  }

  @Test
  void shouldReturnNullWhenPrincipalIsInvalidString() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("invalid");

    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Long result = SecurityUtil.getCurrentUserId();
    assertNull(result);
  }

  @Test
  void shouldReturnNullWhenPrincipalIsNull() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(null);

    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(context);

    Long result = SecurityUtil.getCurrentUserId();
    assertNull(result);
  }
}