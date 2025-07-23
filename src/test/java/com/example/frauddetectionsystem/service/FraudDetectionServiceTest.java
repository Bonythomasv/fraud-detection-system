package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.domain.TransactionDetails;
import com.example.frauddetectionsystem.dto.FraudDetectionResult;
import com.example.frauddetectionsystem.dto.RuleEvaluationResult;
import com.example.frauddetectionsystem.dto.TransactionStatus;
import com.example.frauddetectionsystem.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private RuleEngineService ruleEngineService;
    
    @Mock
    private Executor fraudDetectionExecutor;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Transaction createTestTransaction(String transactionId, String amount, String ipAddress) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setIpAddress(ipAddress);
        
        Map<String, String> originatorDetails = new HashMap<>();
        originatorDetails.put("name", "Test User");
        originatorDetails.put("account", "123456");
        
        Map<String, String> eTransferDetails = new HashMap<>();
        eTransferDetails.put("recipient", "Recipient User");
        eTransferDetails.put("recipientAccount", "654321");
        
        TransactionDetails originator = new TransactionDetails();
        originator.setDetails(originatorDetails);
        
        TransactionDetails eTransfer = new TransactionDetails();
        eTransfer.setDetails(eTransferDetails);
        
        transaction.setOriginatorDetails(originator);
        transaction.setETransferDetails(eTransfer);
        
        return transaction;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void testCheckTransaction_Approved() {
        // Arrange
        Transaction transaction = createTestTransaction("tx123", "500", "192.168.1.1");
        when(transactionRepository.existsByTransactionId("tx123")).thenReturn(false);
        
        RuleEvaluationResult approveResult = new RuleEvaluationResult(
            true, FraudRule.ActionType.APPROVE, "All checks passed", "DEFAULT_APPROVE", 0);
        when(ruleEngineService.evaluateRulesAsync(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(approveResult));

        // Act
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);

        // Assert
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        assertEquals("All checks passed", result.getReason());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCheckTransaction_Rejected_DuplicateId() {
        // Arrange
        Transaction transaction = createTestTransaction("tx123", "500", "192.168.1.1");
        when(transactionRepository.existsByTransactionId("tx123")).thenReturn(true);

        // Act
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);

        // Assert
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
assertEquals("Duplicate transaction ID", result.getReason());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCheckTransaction_Rejected_BlockedIp() {
        // Arrange
        Transaction transaction = createTestTransaction("tx123", "500", "192.0.0.10");
        when(transactionRepository.existsByTransactionId("tx123")).thenReturn(false);
        
        RuleEvaluationResult rejectResult = new RuleEvaluationResult(
            true, FraudRule.ActionType.REJECT, "IP address is blocked", "IP_BLACKLIST_192_SUBNET", 3);
        when(ruleEngineService.evaluateRulesAsync(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(rejectResult));

        // Act
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);

        // Assert
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("IP address is blocked", result.getReason());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCheckTransaction_Rejected_AmountTooHigh() {
        // Arrange
        Transaction transaction = createTestTransaction("tx123", "2500", "192.168.1.1");
        when(transactionRepository.existsByTransactionId("tx123")).thenReturn(false);
        
        RuleEvaluationResult rejectResult = new RuleEvaluationResult(
            true, FraudRule.ActionType.REJECT, "Amount exceeds maximum limit", "AMOUNT_REJECT_THRESHOLD", 1);
        when(ruleEngineService.evaluateRulesAsync(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(rejectResult));

        // Act
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);

        // Assert
        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals("Amount exceeds maximum limit", result.getReason());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCheckTransaction_Hold_MediumAmount() {
        // Arrange
        Transaction transaction = createTestTransaction("tx123", "1500", "192.168.1.1");
        when(transactionRepository.existsByTransactionId("tx123")).thenReturn(false);
        
        RuleEvaluationResult holdResult = new RuleEvaluationResult(
            true, FraudRule.ActionType.HOLD, "Requires manual review", "AMOUNT_HOLD_THRESHOLD", 2);
        when(ruleEngineService.evaluateRulesAsync(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(holdResult));

        // Act
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);

        // Assert
        assertEquals(TransactionStatus.HOLD, result.getStatus());
        assertEquals("Requires manual review", result.getReason());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
