# Fraud Detection System Backend API

## Overview
The Fraud Detection System Backend API evaluates financial transactions for potential fraud based on predefined rules. The system is developed by the Payment Team to ensure secure and reliable transaction processing for high-volume financial operations. The API is implemented as a Spring Boot microservice, accepting JSON requests and returning JSON responses.

## Prerequisites

- Java 17 or higher
- Maven 3.8.0 or higher
- H2 Database (embedded)

## Building the Project

To build the project:
```bash
./mvnw clean package
```

## Running Tests

To run the tests:
```bash
./mvnw test
```

## Running the Application

To start the application:
```bash
./mvnw spring-boot:run
```

The application will start on port 8080. You can access:

- API endpoint: `http://localhost:8080/v1/fraud-check`
- H2 Console: `http://localhost:8080/h2-console`

## API Documentation

The API provides the following endpoints:

### Transaction Fraud Check

**POST /v1/fraud-check**

Request Body:
```json
{
    "transactionId": "string",
    "amount": number,
    "ipAddress": "string",
    "originatorDetails": {
        "name": "string"
    },
    "eTransferDetails": {
        "recipient": "string"
    }
}
```

Response:
```json
{
    "transactionId": "string",
    "status": "APPROVED|HOLD|REJECTED",
    "reason": "string"
}
```

## Fraud Detection Rules

### IP Address Rule

If the transaction originates from an IP address in the range 192.0.0.0 to 192.0.0.255 (checked via X-client-ip header or ipAddress field), the transaction shall be REJECTED with the reason: "Transaction originated from blocked IP range (192.0.0.0 - 192.0.0.255)".

### Amount-Based Rules

If the transaction amount is greater than $2,000, the transaction shall be REJECTED with the reason: "Transaction amount exceeds $2000".

If the transaction amount is between $1,000 and $2,000 (inclusive of $1,000), the transaction shall be placed on HOLD with the reason: "Transaction amount between $1,000 and $2,000 requires review".

If the transaction amount is less than $1,000, the transaction shall be APPROVED with the reason: "Transaction approved".

### Extensibility

The system supports additional fraud detection rules stored in a database, allowing for future expansion to 30,000–40,000 rules. Additional rules may trigger a HOLD status if applicable, with a reason indicating the specific rule triggered.

## Input Validation

- Required fields: `transactionId`, `amount`
- Reject negative amounts: _"Transaction amount cannot be negative"_
- Missing/invalid IPs: proceed if not in blocked range

---

## Non-Functional Requirements

### Performance
- Support up to 10M transactions/day (~115 TPS)
- 95% of responses under 100ms
- Use in-memory cache (e.g., Caffeine) for frequently accessed rules

### Scalability
- Handle 30kâ€“40k rules
- Horizontally scalable

### Reliability
- Target 99.9% uptime
- Clear, actionable error messages

### Security
- Sanitize all inputs
- No logging of sensitive data

### Data Storage
- **Dev/Test DB:** H2 (in-memory)
- **Cache:** In-memory (e.g., Caffeine)
- **Future:** PostgreSQL and Redis for production

---

## Technical Stack

- **Framework:** Spring Boot 3.2.5+
- **Database:** H2 (dev), PostgreSQL (future)
- **Cache:** In-memory (Caffeine), Redis (future)
- **Dependencies:** Spring Web, Spring Data JPA, Spring Cache, H2 Driver
- **Build Tool:** Maven

---

## API Example

**Sample Request**
```json
{
  "transactionId": "txn-12345",
  "amount": 500.00,
  "ipAddress": "10.0.0.1",
  "originatorDetails": "name: John Doe",
  "transferDetails": "recipient: Jane Smith"
}
```

**Sample Response (Approved)**
```json
{
  "transactionId": "txn-12345",
  "status": "APPROVED",
  "reason": "Transaction approved"
}
```

**Sample Response (Rejected - Blocked IP)**
```json
{
  "transactionId": "txn-12348",
  "status": "REJECTED",
  "reason": "Transaction originated from blocked IP range (192.0.0.0 - 192.0.0.255)"
}
```

---

## Testing Requirements

Postman tests for:
- Approved, Hold, Rejected (amount/IP-based)
- Missing IP (valid if amount is okay)
- Missing/invalid fields (400)
- Negative amounts (Rejected)

Tests:
- Unit + edge cases
- Load test for 10M tx/day

---

## Rule Management

The system provides REST endpoints for managing fraud detection rules:

### Rule Management Endpoints

- **GET /api/rules** - List all rules with pagination
- **POST /api/rules** - Create a new rule
- **PUT /api/rules/{id}** - Update an existing rule
- **DELETE /api/rules/{id}** - Delete a rule
- **GET /api/rules/{id}** - Get a specific rule by ID
- **PATCH /api/rules/{id}/toggle** - Enable/disable a rule
- **GET /api/rules/active** - Get all active rules
- **GET /api/rules/active/type/{ruleType}** - Get active rules by type
- **GET /api/rules/stats** - Get rule statistics
- **POST /api/rules/cache/clear** - Clear rule cache

### Current Rule Types

The system currently supports these rule types:

1. **AMOUNT_THRESHOLD** - Evaluate transaction amounts
2. **IP_BLACKLIST** - Block specific IP addresses or patterns
3. **DUPLICATE_TRANSACTION** - Detect duplicate transactions

### Adding a New Rule

To add a new fraud detection rule, you need to modify 2 files:

#### Step 1: Add Rule Type to Enum

Add your new rule type to `FraudRule.RuleType` enum in `FraudRule.java`:

```java
public enum RuleType {
    AMOUNT_THRESHOLD,
    IP_BLACKLIST,
    DUPLICATE_TRANSACTION,
    YOUR_NEW_RULE_TYPE  // Add your new rule type here
}
```

#### Step 2: Implement Rule Evaluation Logic

Add the evaluation logic in `RuleEngineService.java`:

1. **Add case to switch statement** in `evaluateRule()` method:
```java
switch (rule.getRuleType()) {
    case AMOUNT_THRESHOLD:
        return evaluateAmountThreshold(rule, transaction);
    case IP_BLACKLIST:
        return evaluateIpBlacklist(rule, transaction);
    case DUPLICATE_TRANSACTION:
        return evaluateDuplicateTransaction(rule, transaction);
    case YOUR_NEW_RULE_TYPE:  // Add this case
        return evaluateYourNewRuleType(rule, transaction);
    default:
        // ...
}
```

2. **Implement the evaluation method**:
```java
private RuleEvaluationResult evaluateYourNewRuleType(FraudRule rule, Transaction transaction) {
    // Your custom evaluation logic here
    // Access rule properties: rule.getStringValue(), rule.getThresholdValue(), rule.getRuleCondition()
    // Access transaction data: transaction.getAmount(), transaction.getIpAddress(), etc.
    
    boolean triggered = /* your logic */;
    return triggered ? RuleEvaluationResult.triggered(rule) : RuleEvaluationResult.notTriggered();
}
```

### Rule Conditions

Based on the current implementation, these conditions are supported:

**For AMOUNT_THRESHOLD rules:**
- `GREATER_THAN`
- `GREATER_THAN_OR_EQUAL`
- `LESS_THAN`
- `LESS_THAN_OR_EQUAL`

**For IP_BLACKLIST rules:**
- `STARTS_WITH`
- `EQUALS`
- `CONTAINS`
- `REGEX`

### Example: Creating a Rule via API

```json
{
    "ruleName": "HIGH_AMOUNT_THRESHOLD",
    "ruleType": "AMOUNT_THRESHOLD",
    "ruleCondition": "GREATER_THAN",
    "actionType": "REJECT",
    "actionMessage": "Transaction amount exceeds limit",
    "priority": 1,
    "thresholdValue": 2000.00,
    "isActive": true
}
```

```json
{
    "ruleName": "BLOCKED_IP_RANGE",
    "ruleType": "IP_BLACKLIST",
    "ruleCondition": "STARTS_WITH",
    "actionType": "REJECT",
    "actionMessage": "IP address is blocked",
    "priority": 2,
    "stringValue": "192.168.1.",
    "isActive": true
}
```

---

## Assumptions

- `X-client-ip` from trusted proxy/load balancer
- Single Redis instance (if used)
- Stateless API for scalability

---

## Constraints

- Must handle high volume with low latency
- Expandable rule set up to 40k
- Stateless for horizontal scaling

---

## Deliverables

- Source code (Spring Boot microservice)
- Postman collection
- API documentation
- Unit/integration tests

---

## Future Considerations

### Enhanced Rule Engine
- Add ML/behavior-based fraud rules
- Support for complex rule chaining and dependencies
- Rule versioning and A/B testing capabilities
- Dynamic rule priority adjustment based on performance metrics

### Production Infrastructure
- Redis cache & PostgreSQL DB in production
- Support for distributed caching & DB clustering
- Multi-region deployment with data replication
- Auto-scaling based on transaction volume

### Observability & Monitoring
- Structured JSON logging with correlation IDs
- Comprehensive metrics and alerting (Prometheus/Grafana)
- Distributed tracing (Jaeger/Zipkin)
- Rule evaluation performance monitoring
- Fraud detection accuracy tracking

### Administrative Tools
- Web-based UI for rule management and monitoring
- Rule testing and simulation environment
- Bulk rule import/export capabilities
- Audit trail for all rule changes
- Real-time rule performance dashboards

### Security & Compliance
- End-to-end encryption for sensitive data
- PCI DSS compliance features
- Advanced threat detection
- Automated compliance reporting

### Integration & APIs
- Webhook support for real-time notifications
- Integration with external fraud detection services
- Support for multiple data formats (JSON, XML, Avro)