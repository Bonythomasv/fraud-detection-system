package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.dto.FraudDetectionResult;
import com.example.frauddetectionsystem.dto.TransactionStatus;
import com.example.frauddetectionsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public FraudDetectionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public FraudDetectionResult checkTransaction(Transaction transaction) {
        // Check for duplicate transaction ID
        if (transactionRepository.existsByTransactionId(transaction.getTransactionId())) {
            return new FraudDetectionResult(
                transaction.getTransactionId(),
                TransactionStatus.REJECTED,
                "Duplicate transaction ID"
            );
        }

        // Check if IP is in blocked range (192.0.0.0/24)
        if (isIpBlocked(transaction.getIpAddress())) {
            saveTransaction(transaction, TransactionStatus.REJECTED, "IP address is blocked");
            return new FraudDetectionResult(
                transaction.getTransactionId(),
TransactionStatus.REJECTED,
                "IP address is blocked"
            );
        }

        // Check amount thresholds
        BigDecimal amount = transaction.getAmount();
        if (amount == null) {
            saveTransaction(transaction, TransactionStatus.REJECTED, "Invalid amount");
            return new FraudDetectionResult(
                transaction.getTransactionId(),
TransactionStatus.REJECTED,
                "Invalid amount"
            );
        }

        if (amount.compareTo(new BigDecimal("2000")) > 0) {
            saveTransaction(transaction, TransactionStatus.REJECTED, "Amount exceeds maximum limit");
            return new FraudDetectionResult(
                transaction.getTransactionId(),
TransactionStatus.REJECTED,
                "Amount exceeds maximum limit"
            );
        } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            saveTransaction(transaction, TransactionStatus.HOLD, "Requires manual review");
            return new FraudDetectionResult(
                transaction.getTransactionId(),
TransactionStatus.HOLD,
                "Requires manual review"
            );
        }

        // If all checks pass, approve the transaction
        saveTransaction(transaction, TransactionStatus.APPROVED, "Approved");
        return new FraudDetectionResult(
            transaction.getTransactionId(),
TransactionStatus.APPROVED,
            "Approved"
        );
    }

    private boolean isIpBlocked(String ipAddress) {
        // Check if IP is in the blocked range (192.0.0.0/24)
        return ipAddress != null && ipAddress.startsWith("192.0.0.");
    }

    @Transactional
    protected void saveTransaction(Transaction transaction, TransactionStatus status, String statusReason) {
        // Set status and reason before saving
        transaction.setStatus(status);
        transaction.setStatusReason(statusReason);
        transactionRepository.save(transaction);
    }
}
