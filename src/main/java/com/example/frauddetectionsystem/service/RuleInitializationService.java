package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.repository.FraudRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RuleInitializationService implements CommandLineRunner {
    
    private final FraudRuleRepository fraudRuleRepository;
    
    @Autowired
    public RuleInitializationService(FraudRuleRepository fraudRuleRepository) {
        this.fraudRuleRepository = fraudRuleRepository;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        if (fraudRuleRepository.count() == 0) {
            log.info("Initializing fraud detection rules in database...");
            initializeDefaultRules();
            log.info("Fraud detection rules initialization completed. Total rules: {}", 
                fraudRuleRepository.countActiveRules());
        } else {
            log.info("Fraud detection rules already exist. Total active rules: {}", 
                fraudRuleRepository.countActiveRules());
        }
    }
    
    private void initializeDefaultRules() {
        List<FraudRule> defaultRules = List.of(
            // Amount threshold rules (migrated from hardcoded logic)
            createRule(
                "AMOUNT_REJECT_THRESHOLD",
                FraudRule.RuleType.AMOUNT_THRESHOLD,
                "GREATER_THAN",
                FraudRule.ActionType.REJECT,
                "Amount exceeds maximum limit",
                1, // High priority
                new BigDecimal("2000"),
                null
            ),
            
            createRule(
                "AMOUNT_HOLD_THRESHOLD", 
                FraudRule.RuleType.AMOUNT_THRESHOLD,
                "GREATER_THAN_OR_EQUAL",
                FraudRule.ActionType.HOLD,
                "Requires manual review",
                2, // Medium priority
                new BigDecimal("1000"),
                null
            ),
            
            // IP blacklist rule (migrated from hardcoded logic)
            createRule(
                "IP_BLACKLIST_192_SUBNET",
                FraudRule.RuleType.IP_BLACKLIST,
                "STARTS_WITH",
                FraudRule.ActionType.REJECT,
                "IP address is blocked",
                3, // Medium priority
                null,
                "192.0.0."
            )
        );
        
        fraudRuleRepository.saveAll(defaultRules);
        log.info("Saved {} default fraud detection rules", defaultRules.size());
    }
    
    private FraudRule createRule(
            String ruleName,
            FraudRule.RuleType ruleType,
            String ruleCondition,
            FraudRule.ActionType actionType,
            String actionMessage,
            Integer priority,
            BigDecimal thresholdValue,
            String stringValue) {
        
        FraudRule rule = new FraudRule();
        rule.setRuleName(ruleName);
        rule.setRuleType(ruleType);
        rule.setRuleCondition(ruleCondition);
        rule.setActionType(actionType);
        rule.setActionMessage(actionMessage);
        rule.setPriority(priority);
        rule.setIsActive(true);
        rule.setThresholdValue(thresholdValue);
        rule.setStringValue(stringValue);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        return rule;
    }
}
