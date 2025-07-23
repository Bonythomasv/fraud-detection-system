package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.dto.RuleEvaluationResult;
import com.example.frauddetectionsystem.repository.FraudRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @InjectMocks
    private RuleEngineService ruleEngineService;

    private Transaction testTransaction;
    private List<FraudRule> testRules;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setTransactionId("TXN-001");
        testTransaction.setAmount(new BigDecimal("1500"));
        testTransaction.setIpAddress("192.168.1.1");

        // Create test rules
        testRules = Arrays.asList(
            createTestRule("AMOUNT_REJECT", FraudRule.RuleType.AMOUNT_THRESHOLD, 
                "GREATER_THAN", FraudRule.ActionType.REJECT, 1, new BigDecimal("2000"), null),
            createTestRule("AMOUNT_HOLD", FraudRule.RuleType.AMOUNT_THRESHOLD, 
                "GREATER_THAN_OR_EQUAL", FraudRule.ActionType.HOLD, 2, new BigDecimal("1000"), null),
            createTestRule("IP_BLOCK", FraudRule.RuleType.IP_BLACKLIST, 
                "STARTS_WITH", FraudRule.ActionType.REJECT, 3, null, "192.0.0.")
        );
    }

    @Test
    void testEvaluateRulesAsync_AmountHoldRule() throws Exception {
        // Given
        when(fraudRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(testRules);

        // When
        CompletableFuture<RuleEvaluationResult> result = ruleEngineService.evaluateRulesAsync(testTransaction);
        RuleEvaluationResult evaluationResult = result.get();

        // Then
        assertTrue(evaluationResult.isTriggered());
        assertEquals(FraudRule.ActionType.HOLD, evaluationResult.getActionType());
        assertEquals("AMOUNT_HOLD", evaluationResult.getRuleName());
        assertEquals("Requires manual review", evaluationResult.getMessage());
    }

    @Test
    void testEvaluateRulesAsync_AmountRejectRule() throws Exception {
        // Given
        testTransaction.setAmount(new BigDecimal("2500"));
        when(fraudRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(testRules);

        // When
        CompletableFuture<RuleEvaluationResult> result = ruleEngineService.evaluateRulesAsync(testTransaction);
        RuleEvaluationResult evaluationResult = result.get();

        // Then
        assertTrue(evaluationResult.isTriggered());
        assertEquals(FraudRule.ActionType.REJECT, evaluationResult.getActionType());
        assertEquals("AMOUNT_REJECT", evaluationResult.getRuleName());
    }

    @Test
    void testEvaluateRulesAsync_IpBlacklistRule() throws Exception {
        // Given
        testTransaction.setAmount(new BigDecimal("500")); // Below thresholds
        testTransaction.setIpAddress("192.0.0.100");
        when(fraudRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(testRules);

        // When
        CompletableFuture<RuleEvaluationResult> result = ruleEngineService.evaluateRulesAsync(testTransaction);
        RuleEvaluationResult evaluationResult = result.get();

        // Then
        assertTrue(evaluationResult.isTriggered());
        assertEquals(FraudRule.ActionType.REJECT, evaluationResult.getActionType());
        assertEquals("IP_BLOCK", evaluationResult.getRuleName());
    }

    @Test
    void testEvaluateRulesAsync_NoRulesTriggered() throws Exception {
        // Given
        testTransaction.setAmount(new BigDecimal("500")); // Below thresholds
        testTransaction.setIpAddress("10.0.0.1"); // Not blocked
        when(fraudRuleRepository.findActiveRulesOrderedByPriority()).thenReturn(testRules);

        // When
        CompletableFuture<RuleEvaluationResult> result = ruleEngineService.evaluateRulesAsync(testTransaction);
        RuleEvaluationResult evaluationResult = result.get();

        // Then
        assertTrue(evaluationResult.isTriggered());
        assertEquals(FraudRule.ActionType.APPROVE, evaluationResult.getActionType());
        assertEquals("DEFAULT_APPROVE", evaluationResult.getRuleName());
    }

    private FraudRule createTestRule(String name, FraudRule.RuleType type, String condition, 
                                   FraudRule.ActionType action, int priority, 
                                   BigDecimal threshold, String stringValue) {
        FraudRule rule = new FraudRule();
        rule.setId((long) priority);
        rule.setRuleName(name);
        rule.setRuleType(type);
        rule.setRuleCondition(condition);
        rule.setActionType(action);
        rule.setActionMessage(action == FraudRule.ActionType.HOLD ? "Requires manual review" : 
                             action == FraudRule.ActionType.REJECT ? "Transaction rejected" : "Approved");
        rule.setPriority(priority);
        rule.setIsActive(true);
        rule.setThresholdValue(threshold);
        rule.setStringValue(stringValue);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return rule;
    }
}
