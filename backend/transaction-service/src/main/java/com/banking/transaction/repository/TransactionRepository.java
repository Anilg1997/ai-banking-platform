package com.banking.transaction.repository;

import com.banking.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    // Find transactions where user is sender OR receiver
    @Query("SELECT t FROM Transaction t WHERE t.fromUserId = :userId OR t.toUserId = :userId ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserInvolved(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromUserId = :userId OR t.toUserId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserInvolved(@Param("userId") String userId);

    // By account
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountInvolved(@Param("accountId") String accountId);

    // By type
    Page<Transaction> findByFromUserIdAndType(String fromUserId, Transaction.TransactionType type, Pageable pageable);
    Page<Transaction> findByToUserIdAndType(String toUserId, Transaction.TransactionType type, Pageable pageable);

    // By status
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    // Date range
    @Query("SELECT t FROM Transaction t WHERE (t.fromUserId = :userId OR t.toUserId = :userId) " +
           "AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByUserAndDateRange(@Param("userId") String userId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // Recent transactions for a user
    @Query("SELECT t FROM Transaction t WHERE (t.fromUserId = :userId OR t.toUserId = :userId) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUser(@Param("userId") String userId, Pageable pageable);

    long countByFromUserIdOrToUserId(String fromUserId, String toUserId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toUserId = :userId AND t.status = 'COMPLETED'")
    double totalReceivedByUser(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromUserId = :userId AND t.status = 'COMPLETED'")
    double totalSentByUser(@Param("userId") String userId);
}
