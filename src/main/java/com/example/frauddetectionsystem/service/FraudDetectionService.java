package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.dto.FraudDetectionResult;
import com.example.frauddetectionsystem.dto.RuleEvaluationResult;
import com.example.frauddetectionsystem.dto.TransactionStatus;
import com.example.frauddetectionsystem.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final RuleEngineService ruleEngineService;
    private final Executor fraudDetectionExecutor;

    @Autowired
    public FraudDetectionService(
            TransactionRepository transactionRepository,
            RuleEngineService ruleEngineService,
            @Qualifier("fraudDetectionExecutor") Executor fraudDetectionExecutor) {
        this.transactionRepository = transactionRepository;
        this.ruleEngineService = ruleEngineService;
        this.fraudDetectionExecutor = fraudDetectionExecutor;
    }

    @Transactional
    public FraudDetectionResult checkTransaction(Transaction transaction) {
        log.info("Starting fraud detection for transaction: {}", transaction.getTransactionId());
        
        try {
            // Quick duplicate check first (synchronous)
            if (transactionRepository.existsByTransactionId(transaction.getTransactionId())) {
                log.warn("Duplicate transaction detected: {}", transaction.getTransactionId());
                return new FraudDetectionResult(
                    transaction.getTransactionId(),
                    TransactionStatus.REJECTED,
                    "Duplicate transaction ID"
                );
            }

            // Evaluate rules asynchronously and wait for result
            CompletableFuture<RuleEvaluationResult> ruleEvaluation = 
                ruleEngineService.evaluateRulesAsync(transaction);
            
            // Wait for rule evaluation with timeout
            RuleEvaluationResult result = ruleEvaluation.get();
            
            // Convert rule evaluation result to fraud detection result
            TransactionStatus status = mapActionToStatus(result.getActionType());
            String message = result.getMessage();
            
            // Save transaction with determined status
            saveTransaction(transaction, status, message);
            
            log.info("Fraud detection completed for transaction {}: {} - {}", 
                transaction.getTransactionId(), status, message);
            
            return new FraudDetectionResult(
                transaction.getTransactionId(),
                status,
                message
            );
            
        } catch (Exception e) {
            log.error("Error during fraud detection for transaction {}: {}", 
                transaction.getTransactionId(), e.getMessage(), e);
            
            // Fail safe - reject on error
            saveTransaction(transaction, TransactionStatus.REJECTED, "System error during fraud detection");
            return new FraudDetectionResult(
                transaction.getTransactionId(),
                TransactionStatus.REJECTED,
                "System error during fraud detection"
            );
        }
    }
    
    @Async("fraudDetectionExecutor")
    public CompletableFuture<FraudDetectionResult> checkTransactionAsync(Transaction transaction) {
        return CompletableFuture.supplyAsync(() -> checkTransaction(transaction), fraudDetectionExecutor);
    }
    
    private TransactionStatus mapActionToStatus(FraudRule.ActionType actionType) {
        if (actionType == null) {
            return TransactionStatus.REJECTED;
        }
        
        return switch (actionType) {
            case APPROVE -> TransactionStatus.APPROVED;
            case REJECT -> TransactionStatus.REJECTED;
            case HOLD, FLAG_FOR_REVIEW -> TransactionStatus.HOLD;
        };
    }

    @Transactional
    protected void saveTransaction(Transaction transaction, TransactionStatus status, String statusReason) {
        // Set status and reason before saving
        transaction.setStatus(status);
        transaction.setStatusReason(statusReason);
        transactionRepository.save(transaction);
        
        log.debug("Transaction {} saved with status: {} - {}", 
            transaction.getTransactionId(), status, statusReason);
    }
}
