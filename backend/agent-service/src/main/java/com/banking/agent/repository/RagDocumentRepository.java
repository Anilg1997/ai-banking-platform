package com.banking.agent.repository;

import com.banking.agent.model.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RagDocumentRepository extends JpaRepository<RagDocument, UUID> {

    List<RagDocument> findByCategory(String category);

    List<RagDocument> findBySource(String source);

    List<RagDocument> findByCategoryAndSource(String category, String source);
}
