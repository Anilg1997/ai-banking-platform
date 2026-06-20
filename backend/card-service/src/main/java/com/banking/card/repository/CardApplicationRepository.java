package com.banking.card.repository;

import com.banking.card.model.CardApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardApplicationRepository extends JpaRepository<CardApplication, String> {

    List<CardApplication> findByUserId(String userId);

    List<CardApplication> findByStatus(CardApplication.ApplicationStatus status);

    Optional<CardApplication> findByUserIdAndStatus(String userId, CardApplication.ApplicationStatus status);

    List<CardApplication> findByStatusOrderBySubmittedAtDesc(CardApplication.ApplicationStatus status);
}
