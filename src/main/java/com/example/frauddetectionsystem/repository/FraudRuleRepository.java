package com.example.frauddetectionsystem.repository;

import com.example.frauddetectionsystem.domain.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {
    
    @Query("SELECT fr FROM FraudRule fr WHERE fr.isActive = true ORDER BY fr.priority ASC")
    List<FraudRule> findActiveRulesOrderedByPriority();
    
    @Query("SELECT fr FROM FraudRule fr WHERE fr.isActive = true AND fr.ruleType = :ruleType ORDER BY fr.priority ASC")
    List<FraudRule> findActiveRulesByTypeOrderedByPriority(FraudRule.RuleType ruleType);
    
    List<FraudRule> findByRuleNameAndIsActive(String ruleName, Boolean isActive);
    
    @Query("SELECT COUNT(fr) FROM FraudRule fr WHERE fr.isActive = true")
    long countActiveRules();
}
