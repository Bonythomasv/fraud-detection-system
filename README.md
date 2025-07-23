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

## Future Considerations

- Add ML/behavior-based fraud rules
- Redis cache & PostgreSQL DB in production
- Monitoring & rule evaluation logging
- Support for distributed caching & DB clustering

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
- Config: H2 + in-memory cache (Caffeine)