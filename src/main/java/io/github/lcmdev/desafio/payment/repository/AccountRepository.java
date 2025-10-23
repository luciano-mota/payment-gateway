package io.github.lcmdev.desafio.payment.repository;

import io.github.lcmdev.desafio.payment.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
