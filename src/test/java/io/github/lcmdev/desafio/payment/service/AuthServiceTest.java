package io.github.lcmdev.desafio.payment.service;

import static io.github.lcmdev.desafio.payment.mock.AuthServiceMock.createUserMock;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lcmdev.desafio.payment.controller.dto.request.RegisterRequestDTO;
import io.github.lcmdev.desafio.payment.repository.UserRepository;
import io.github.lcmdev.desafio.payment.security.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Test
  void shouldRegisterUserWithSuccess() {
    when(userRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(any())).thenReturn("asdf1234");
    when(userRepository.save(any())).thenReturn(createUserMock().get());

    var register = authService.register(
        new RegisterRequestDTO("test", "74770769008", "test@test.com", "test123"));

    assertAll(
        () -> assertNotNull(register),
        () -> verify(userRepository).findByCpf(any()),
        () -> verify(userRepository).findByEmail(any()),
        () -> verify(passwordEncoder).encode(any()),
        () -> verify(userRepository).save(any())
    );
  }

  @Test
  void shouldExceptionInRegisterWhenCpfIsExists() {
    when(userRepository.findByCpf(any())).thenReturn(createUserMock());

    var exception = assertThrows(IllegalArgumentException.class,
        () -> authService.register(
            new RegisterRequestDTO("test", "74770769008", "test@test.com", "test123")
        )
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("CPF already registered", exception.getMessage()),
        () -> verify(userRepository).findByCpf(any())
    );
  }

  @Test
  void shouldExceptionInRegisterWhenEmailIsExists() {
    when(userRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(any())).thenReturn(createUserMock());

    var exception = assertThrows(IllegalArgumentException.class,
        () -> authService.register(
            new RegisterRequestDTO("test", "74770769008", "test@test.com", "test123")
          )
        );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Email already registered", exception.getMessage()),
        () -> verify(userRepository).findByCpf(any()),
        () -> verify(userRepository).findByEmail(any())
    );
  }

  @Test
  void shouldLoginUserWithSuccess() {
    when(userRepository.findByCpf(any())).thenReturn(createUserMock());
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    when(jwtUtil.generateToken(any())).thenReturn("test");

    var login = authService.login("74770769008", "test123");

    assertAll(
        () -> assertNotNull(login),
        () -> verify(userRepository).findByCpf(any()),
        () -> verify(passwordEncoder).matches(any(), any()),
        () -> verify(jwtUtil).generateToken(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenUserLoginDoesNotExists() {
    when(userRepository.findByCpf(any())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class,
        () -> authService.login("74770769008", "test123")
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Invalid credentials", exception.getMessage()),
        () -> verify(userRepository).findByCpf(any()),
        () -> verify(userRepository).findByEmail(any())
    );
  }

  @Test
  void shouldReturnExceptionWhenMatchesInvalid() {
    when(userRepository.findByCpf(any())).thenReturn(createUserMock());
    when(passwordEncoder.matches(any(), any())).thenReturn(false);

    var exception = assertThrows(IllegalArgumentException.class,
        () -> authService.login("74770769008", "test123")
    );

    assertAll(
        () -> assertNotNull(exception),
        () -> assertEquals("Invalid credentials", exception.getMessage()),
        () -> verify(userRepository).findByCpf(any()),
        () -> verify(passwordEncoder).matches(any(), any())
    );
  }
}