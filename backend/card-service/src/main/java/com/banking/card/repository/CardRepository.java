package com.banking.card.repository;

import com.banking.card.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByUserId(String userId);

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByUserIdAndStatus(String userId, Card.CardStatus status);

    List<Card> findByStatus(Card.CardStatus status);

    long countByUserId(String userId);

    List<Card> findByExpiryDateBefore(LocalDate date);
}
