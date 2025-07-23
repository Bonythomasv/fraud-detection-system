package com.example.frauddetectionsystem.repository;

import com.example.frauddetectionsystem.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByTransactionId(String transactionId);
}
