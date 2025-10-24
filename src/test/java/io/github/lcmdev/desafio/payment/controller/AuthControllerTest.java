package io.github.lcmdev.desafio.payment.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lcmdev.desafio.payment.controller.dto.request.LoginRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.RegisterRequestDTO;
import io.github.lcmdev.desafio.payment.model.User;
import io.github.lcmdev.desafio.payment.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @Test
  void shouldRegisterWithSuccess() throws Exception {
    var request = new RegisterRequestDTO(
        "John Doe",
        "52998224725",
        "john.doe@mail.com",
        "secret"
    );

    var user = Mockito.mock(User.class);
    when(user.getId()).thenReturn(100L);
    when(authService.register(any(RegisterRequestDTO.class))).thenReturn(user);

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", notNullValue()));
  }

  @Test
  void shouldReturnBadRequestWhenRegisterFails() throws Exception {
    var request = new RegisterRequestDTO(
        "Jane Doe",
        "52998224725",
        "jane.doe@mail.com",
        "secret"
    );
    when(authService.register(any(RegisterRequestDTO.class)))
        .thenThrow(new IllegalArgumentException("CPF already registered"));

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("CPF already registered")));
  }

  @Test
  void shouldLoginWithSuccess() throws Exception {
    var request = new LoginRequestDTO("john.doe@mail.com", "secret");
    when(authService.login(eq("john.doe@mail.com"), eq("secret")))
        .thenReturn("jwt-token-123");

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", is("jwt-token-123")));
  }

  @Test
  void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
    var request = new LoginRequestDTO("john.doe@mail.com", "wrong");

    when(authService.login(eq("john.doe@mail.com"), eq("wrong")))
        .thenThrow(new IllegalArgumentException("Credenciais inválidas"));

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error", is("Credenciais inválidas")));
  }
}