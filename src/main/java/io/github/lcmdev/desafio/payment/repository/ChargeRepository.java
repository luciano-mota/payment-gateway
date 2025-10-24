package io.github.lcmdev.desafio.payment.repository;

import io.github.lcmdev.desafio.payment.enums.ChargeStatusEnum;
import io.github.lcmdev.desafio.payment.model.Charge;
import io.github.lcmdev.desafio.payment.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByOrigin(User origin);

    List<Charge> findByDestination(User destination);

    List<Charge> findByOriginAndStatus(User origin, ChargeStatusEnum status);

    List<Charge> findByDestinationAndStatus(User destination, ChargeStatusEnum status);
}