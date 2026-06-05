package com.banking.account.repository;

import com.banking.account.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(String userId);

    List<Account> findByUserIdAndStatus(String userId, Account.AccountStatus status);

    List<Account> findByStatus(Account.AccountStatus status);

    boolean existsByAccountNumber(String accountNumber);

    long countByUserId(String userId);
}
