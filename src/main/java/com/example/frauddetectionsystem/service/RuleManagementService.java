package com.example.frauddetectionsystem.service;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.repository.FraudRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RuleManagementService {
    
    private final FraudRuleRepository fraudRuleRepository;
    
    @Autowired
    public RuleManagementService(FraudRuleRepository fraudRuleRepository) {
        this.fraudRuleRepository = fraudRuleRepository;
    }
    
    @Transactional
    @CacheEvict(value = {"activeRules", "rulesByType"}, allEntries = true)
    public FraudRule createRule(FraudRule rule) {
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }
        
        FraudRule savedRule = fraudRuleRepository.save(rule);
        log.info("Created new fraud rule: {} with ID: {}", rule.getRuleName(), savedRule.getId());
        return savedRule;
    }
    
    @Transactional
    @CacheEvict(value = {"activeRules", "rulesByType"}, allEntries = true)
    public FraudRule updateRule(Long ruleId, FraudRule updatedRule) {
        Optional<FraudRule> existingRule = fraudRuleRepository.findById(ruleId);
        if (existingRule.isEmpty()) {
            throw new IllegalArgumentException("Rule not found with ID: " + ruleId);
        }
        
        FraudRule rule = existingRule.get();
        rule.setRuleName(updatedRule.getRuleName());
        rule.setRuleType(updatedRule.getRuleType());
        rule.setRuleCondition(updatedRule.getRuleCondition());
        rule.setActionType(updatedRule.getActionType());
        rule.setActionMessage(updatedRule.getActionMessage());
        rule.setPriority(updatedRule.getPriority());
        rule.setIsActive(updatedRule.getIsActive());
        rule.setThresholdValue(updatedRule.getThresholdValue());
        rule.setStringValue(updatedRule.getStringValue());
        rule.setUpdatedAt(LocalDateTime.now());
        
        FraudRule savedRule = fraudRuleRepository.save(rule);
        log.info("Updated fraud rule: {} with ID: {}", rule.getRuleName(), savedRule.getId());
        return savedRule;
    }
    
    @Transactional
    @CacheEvict(value = {"activeRules", "rulesByType"}, allEntries = true)
    public void deleteRule(Long ruleId) {
        if (!fraudRuleRepository.existsById(ruleId)) {
            throw new IllegalArgumentException("Rule not found with ID: " + ruleId);
        }
        
        fraudRuleRepository.deleteById(ruleId);
        log.info("Deleted fraud rule with ID: {}", ruleId);
    }
    
    @Transactional
    @CacheEvict(value = {"activeRules", "rulesByType"}, allEntries = true)
    public FraudRule toggleRuleStatus(Long ruleId) {
        Optional<FraudRule> existingRule = fraudRuleRepository.findById(ruleId);
        if (existingRule.isEmpty()) {
            throw new IllegalArgumentException("Rule not found with ID: " + ruleId);
        }
        
        FraudRule rule = existingRule.get();
        rule.setIsActive(!rule.getIsActive());
        rule.setUpdatedAt(LocalDateTime.now());
        
        FraudRule savedRule = fraudRuleRepository.save(rule);
        log.info("Toggled fraud rule status: {} - Active: {}", rule.getRuleName(), rule.getIsActive());
        return savedRule;
    }
    
    @Transactional(readOnly = true)
    public Optional<FraudRule> getRuleById(Long ruleId) {
        return fraudRuleRepository.findById(ruleId);
    }
    
    @Transactional(readOnly = true)
    public Page<FraudRule> getAllRules(Pageable pageable) {
        return fraudRuleRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "activeRules", key = "'all'")
    public List<FraudRule> getActiveRules() {
        return fraudRuleRepository.findActiveRulesOrderedByPriority();
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "rulesByType", key = "#ruleType")
    public List<FraudRule> getActiveRulesByType(FraudRule.RuleType ruleType) {
        return fraudRuleRepository.findActiveRulesByTypeOrderedByPriority(ruleType);
    }
    
    @Transactional(readOnly = true)
    public long getActiveRuleCount() {
        return fraudRuleRepository.countActiveRules();
    }
    
    @CacheEvict(value = {"activeRules", "rulesByType"}, allEntries = true)
    public void clearRuleCache() {
        log.info("Cleared fraud rule cache");
    }
}
