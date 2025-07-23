package com.example.frauddetectionsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;
    
    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;
    
    @Column(name = "rule_condition", nullable = false, length = 1000)
    private String ruleCondition;
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    @Column(name = "action_message")
    private String actionMessage;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "threshold_value")
    private BigDecimal thresholdValue;
    
    @Column(name = "string_value")
    private String stringValue;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum RuleType {
        AMOUNT_THRESHOLD,
        IP_BLACKLIST,
        DUPLICATE_TRANSACTION
    }
    
    public enum ActionType {
        APPROVE,
        REJECT,
        HOLD,
        FLAG_FOR_REVIEW
    }
}
