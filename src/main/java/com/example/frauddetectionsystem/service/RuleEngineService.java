package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.dto.RuleEvaluationResult;
import com.example.frauddetectionsystem.repository.FraudRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class RuleEngineService {
    
    private final FraudRuleRepository fraudRuleRepository;
    private final Executor ruleExecutor;
    
    @Autowired
    public RuleEngineService(FraudRuleRepository fraudRuleRepository) {
        this.fraudRuleRepository = fraudRuleRepository;
        // Create a thread pool for rule evaluation - size based on expected load
        this.ruleExecutor = Executors.newFixedThreadPool(20);
    }
    
    @Cacheable(value = "activeRules", key = "'all'")
    public List<FraudRule> getActiveRules() {
        log.debug("Fetching active rules from database");
        return fraudRuleRepository.findActiveRulesOrderedByPriority();
    }
    
    public CompletableFuture<RuleEvaluationResult> evaluateRulesAsync(Transaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<FraudRule> rules = getActiveRules();
                log.debug("Evaluating {} rules for transaction {}", rules.size(), transaction.getTransactionId());
                
                // Evaluate rules in priority order - stop at first triggered rule
                for (FraudRule rule : rules) {
                    RuleEvaluationResult result = evaluateRule(rule, transaction);
                    if (result.isTriggered()) {
                        log.info("Rule '{}' triggered for transaction {}: {}", 
                            rule.getRuleName(), transaction.getTransactionId(), result.getMessage());
                        return result;
                    }
                }
                
                // No rules triggered - approve
                return new RuleEvaluationResult(true, FraudRule.ActionType.APPROVE, "All checks passed", "DEFAULT_APPROVE", 0);
                
            } catch (Exception e) {
                log.error("Error evaluating rules for transaction {}: {}", transaction.getTransactionId(), e.getMessage(), e);
                // Fail safe - reject on error
                return new RuleEvaluationResult(true, FraudRule.ActionType.REJECT, "System error during rule evaluation", "ERROR_HANDLER", -1);
            }
        }, ruleExecutor);
    }
    
    private RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            switch (rule.getRuleType()) {
                case AMOUNT_THRESHOLD:
                    return evaluateAmountThreshold(rule, transaction);
                case IP_BLACKLIST:
                    return evaluateIpBlacklist(rule, transaction);
                case DUPLICATE_TRANSACTION:
                    return evaluateDuplicateTransaction(rule, transaction);
                default:
                    log.warn("Unknown rule type: {} for rule: {}", rule.getRuleType(), rule.getRuleName());
                    return RuleEvaluationResult.notTriggered();
            }
        } catch (Exception e) {
            log.error("Error evaluating rule '{}': {}", rule.getRuleName(), e.getMessage(), e);
            return RuleEvaluationResult.notTriggered();
        }
    }
    
    private RuleEvaluationResult evaluateAmountThreshold(FraudRule rule, Transaction transaction) {
        if (transaction.getAmount() == null || rule.getThresholdValue() == null) {
            return RuleEvaluationResult.notTriggered();
        }
        
        // Rule condition format: "GREATER_THAN", "GREATER_THAN_OR_EQUAL", "LESS_THAN", "LESS_THAN_OR_EQUAL"
        String condition = rule.getRuleCondition();
        BigDecimal amount = transaction.getAmount();
        BigDecimal threshold = rule.getThresholdValue();
        
        boolean triggered = switch (condition) {
            case "GREATER_THAN" -> amount.compareTo(threshold) > 0;
            case "GREATER_THAN_OR_EQUAL" -> amount.compareTo(threshold) >= 0;
            case "LESS_THAN" -> amount.compareTo(threshold) < 0;
            case "LESS_THAN_OR_EQUAL" -> amount.compareTo(threshold) <= 0;
            default -> false;
        };
        
        return triggered ? RuleEvaluationResult.triggered(rule) : RuleEvaluationResult.notTriggered();
    }
    
    private RuleEvaluationResult evaluateIpBlacklist(FraudRule rule, Transaction transaction) {
        if (transaction.getIpAddress() == null || rule.getStringValue() == null) {
            return RuleEvaluationResult.notTriggered();
        }
        
        String ipAddress = transaction.getIpAddress();
        String blacklistPattern = rule.getStringValue();
        
        // Rule condition format: "STARTS_WITH", "EQUALS", "CONTAINS", "REGEX"
        String condition = rule.getRuleCondition();
        
        boolean triggered = switch (condition) {
            case "STARTS_WITH" -> ipAddress.startsWith(blacklistPattern);
            case "EQUALS" -> ipAddress.equals(blacklistPattern);
            case "CONTAINS" -> ipAddress.contains(blacklistPattern);
            case "REGEX" -> ipAddress.matches(blacklistPattern);
            default -> false;
        };
        
        return triggered ? RuleEvaluationResult.triggered(rule) : RuleEvaluationResult.notTriggered();
    }
    
    private RuleEvaluationResult evaluateDuplicateTransaction(FraudRule rule, Transaction transaction) {
        // This would typically check against existing transactions
        // For now, we'll implement a simple check
        // In a real implementation, this would query the transaction repository
        return RuleEvaluationResult.notTriggered();
    }
}
