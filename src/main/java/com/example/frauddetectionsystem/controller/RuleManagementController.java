package com.example.frauddetectionsystem.controller;

import com.example.frauddetectionsystem.domain.FraudRule;
import com.example.frauddetectionsystem.service.RuleManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rules")
@Slf4j
public class RuleManagementController {
    
    private final RuleManagementService ruleManagementService;
    
    @Autowired
    public RuleManagementController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }
    
    @PostMapping
    public ResponseEntity<FraudRule> createRule(@Valid @RequestBody FraudRule rule) {
        try {
            FraudRule createdRule = ruleManagementService.createRule(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
        } catch (Exception e) {
            log.error("Error creating rule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{ruleId}")
    public ResponseEntity<FraudRule> updateRule(
            @PathVariable Long ruleId, 
            @Valid @RequestBody FraudRule rule) {
        try {
            FraudRule updatedRule = ruleManagementService.updateRule(ruleId, rule);
            return ResponseEntity.ok(updatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating rule {}: {}", ruleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long ruleId) {
        try {
            ruleManagementService.deleteRule(ruleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting rule {}: {}", ruleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{ruleId}/toggle")
    public ResponseEntity<FraudRule> toggleRuleStatus(@PathVariable Long ruleId) {
        try {
            FraudRule updatedRule = ruleManagementService.toggleRuleStatus(ruleId);
            return ResponseEntity.ok(updatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error toggling rule status {}: {}", ruleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{ruleId}")
    public ResponseEntity<FraudRule> getRuleById(@PathVariable Long ruleId) {
        Optional<FraudRule> rule = ruleManagementService.getRuleById(ruleId);
        return rule.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<Page<FraudRule>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FraudRule> rules = ruleManagementService.getAllRules(pageable);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            log.error("Error fetching rules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<FraudRule>> getActiveRules() {
        try {
            List<FraudRule> activeRules = ruleManagementService.getActiveRules();
            return ResponseEntity.ok(activeRules);
        } catch (Exception e) {
            log.error("Error fetching active rules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/active/type/{ruleType}")
    public ResponseEntity<List<FraudRule>> getActiveRulesByType(@PathVariable FraudRule.RuleType ruleType) {
        try {
            List<FraudRule> rules = ruleManagementService.getActiveRulesByType(ruleType);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            log.error("Error fetching active rules by type {}: {}", ruleType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<RuleStats> getRuleStats() {
        try {
            long activeRuleCount = ruleManagementService.getActiveRuleCount();
            RuleStats stats = new RuleStats(activeRuleCount);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching rule stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        try {
            ruleManagementService.clearRuleCache();
            return ResponseEntity.ok("Cache cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing cache");
        }
    }
    
    public static class RuleStats {
        private final long activeRuleCount;
        
        public RuleStats(long activeRuleCount) {
            this.activeRuleCount = activeRuleCount;
        }
        
        public long getActiveRuleCount() {
            return activeRuleCount;
        }
    }
}
