package com.banking.card.repository;

import com.banking.card.model.CardTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransaction, String> {

    List<CardTransaction> findByCardId(String cardId);

    List<CardTransaction> findByUserId(String userId);

    List<CardTransaction> findByCardIdAndStatus(String cardId, CardTransaction.TransactionStatus status);

    Page<CardTransaction> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<CardTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<CardTransaction> findByTransactionRef(String transactionRef);

    Page<CardTransaction> findByCardIdAndMerchantCategory(String cardId, CardTransaction.MerchantCategory category, Pageable pageable);

    long countByCardId(String cardId);

    Page<CardTransaction> findByCardId(String cardId, Pageable pageable);

    List<CardTransaction> findByCardIdAndCreatedAtBetween(String cardId, LocalDateTime start, LocalDateTime end);
}
