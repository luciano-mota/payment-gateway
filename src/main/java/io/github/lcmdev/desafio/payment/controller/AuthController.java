package io.github.lcmdev.desafio.payment.controller;

import io.github.lcmdev.desafio.payment.controller.dto.request.LoginRequestDTO;
import io.github.lcmdev.desafio.payment.controller.dto.request.RegisterRequestDTO;
import io.github.lcmdev.desafio.payment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO requestDTO) {
    try {
      var user = authService.register(requestDTO);
      return ResponseEntity.ok(Map.of("id", user.getId()));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
    try {
      var token = authService.login(loginRequestDTO.login(), loginRequestDTO.password());
      return ResponseEntity.ok(Map.of("token", token));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
    }
  }
}