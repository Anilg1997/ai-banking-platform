package com.banking.card.repository;

import com.banking.card.model.CardStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardStatementRepository extends JpaRepository<CardStatement, String> {

    List<CardStatement> findByCardIdOrderByStatementDateDesc(String cardId);

    Optional<CardStatement> findByCardIdAndStatementDate(String cardId, LocalDate statementDate);

    List<CardStatement> findByUserIdAndPaymentStatus(String userId, CardStatement.PaymentStatus paymentStatus);

    Optional<CardStatement> findTopByCardIdOrderByStatementDateDesc(String cardId);
}
