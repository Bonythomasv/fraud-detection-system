-- Fraud Rules Table
CREATE TABLE IF NOT EXISTS fraud_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    rule_type VARCHAR(50) NOT NULL,
    rule_condition VARCHAR(1000) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_message VARCHAR(500),
    priority INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    threshold_value DECIMAL(19,2),
    string_value VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create indexes separately for H2 compatibility
CREATE INDEX IF NOT EXISTS idx_fraud_rules_active_priority ON fraud_rules (is_active, priority);
CREATE INDEX IF NOT EXISTS idx_fraud_rules_type_active ON fraud_rules (rule_type, is_active);
CREATE INDEX IF NOT EXISTS idx_fraud_rules_name ON fraud_rules (rule_name);

-- Add comments for documentation
COMMENT ON TABLE fraud_rules IS 'Stores configurable fraud detection rules';
COMMENT ON COLUMN fraud_rules.rule_name IS 'Unique identifier for the rule';
COMMENT ON COLUMN fraud_rules.rule_type IS 'Type of rule: AMOUNT_THRESHOLD, IP_BLACKLIST, etc.';
COMMENT ON COLUMN fraud_rules.rule_condition IS 'Condition logic for rule evaluation';
COMMENT ON COLUMN fraud_rules.action_type IS 'Action to take: APPROVE, REJECT, HOLD, FLAG_FOR_REVIEW';
COMMENT ON COLUMN fraud_rules.priority IS 'Rule execution priority (lower number = higher priority)';
COMMENT ON COLUMN fraud_rules.threshold_value IS 'Numeric threshold for amount-based rules';
COMMENT ON COLUMN fraud_rules.string_value IS 'String value for pattern-based rules';
