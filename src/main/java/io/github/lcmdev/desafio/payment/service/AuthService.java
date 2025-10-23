package io.github.lcmdev.desafio.payment.service;

import io.github.lcmdev.desafio.payment.controller.dto.request.RegisterRequestDTO;
import io.github.lcmdev.desafio.payment.model.Account;
import io.github.lcmdev.desafio.payment.model.User;
import io.github.lcmdev.desafio.payment.repository.UserRepository;
import io.github.lcmdev.desafio.payment.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequestDTO requestDTO) {
        if (userRepository.findByCpf(requestDTO.cpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        if (userRepository.findByEmail(requestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        var user = completeUserRegistration(requestDTO);

        return userRepository.save(user);
    }

    public String login(String login, String password) {
        var user = userRepository.findByCpf(login)
                .or(() -> userRepository.findByEmail(login));

        if (user.isEmpty()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (!passwordEncoder.matches(password, user.get().getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return jwtUtil.generateToken(user.get().getId());
    }

    private User completeUserRegistration( RegisterRequestDTO dto) {
        var user = new User();
        var account = new Account();

        user.setName(dto.name());
        user.setCpf(dto.cpf());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        account.setUser(user);
        user.setAccount(account);
        return user;
    }
}
