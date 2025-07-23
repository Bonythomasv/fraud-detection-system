package com.example.frauddetectionsystem.dto;

import com.example.frauddetectionsystem.domain.FraudRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {
    private boolean triggered;
    private FraudRule.ActionType actionType;
    private String message;
    private String ruleName;
    private int priority;
    
    public static RuleEvaluationResult notTriggered() {
        return new RuleEvaluationResult(false, null, null, null, Integer.MAX_VALUE);
    }
    
    public static RuleEvaluationResult triggered(FraudRule rule) {
        return new RuleEvaluationResult(
            true, 
            rule.getActionType(), 
            rule.getActionMessage(), 
            rule.getRuleName(),
            rule.getPriority()
        );
    }
}
