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

The system shall validate required fields (transactionId, amount) and return a 400 Bad Request response with a REJECTED status and appropriate reason if validation fails.
The system shall reject transactions with a negative amount, returning a REJECTED status with the reason: "Transaction amount cannot be negative".
The system shall handle missing or invalid IP addresses gracefully, proceeding with rule evaluation if the IP is not in the blocked range.

3. Non-Functional Requirements
3.1 Performance

The system shall handle up to 10 million transactions per day (approximately 115 transactions per second).
The API shall achieve low-latency responses, targeting under 100ms for 95% of requests under normal load.
Caching (using Mem-cache/Redis) shall be implemented to optimize rule evaluation for frequently accessed rules.

3.2 Scalability

The system shall support a rule base of 30,000–40,000 fraud detection rules stored in a database.
The architecture shall support horizontal scaling to handle increased transaction volumes.

3.3 Reliability

The system shall ensure high availability, targeting 99.9% uptime.
Error handling shall provide clear and actionable error messages in the response.

3.4 Security

The API shall validate and sanitize all inputs to prevent injection attacks.
Sensitive data (e.g., originatorDetails, transferDetails) shall be handled securely, with no unnecessary logging or exposure.

3.5 Data Storage

Database: H2 in-memory database shall be used for initial development and testing, with the option to switch to a persistent database (e.g., PostgreSQL) in production.
Caching: Redis shall be used as a Mem-cache to store frequently accessed fraud rules, with a configurable time-to-live (TTL) for cache entries (e.g., 10 minutes).
The system shall support efficient querying of rules to minimize database load.

4. Technical Stack

Framework: Spring Boot (version 3.2.5 or later) for building the microservice.
Database: H2 (in-memory for development/testing), with potential for PostgreSQL in production.
Caching: Redis for high-performance rule caching.
Dependencies:
Spring Web for REST API.
Spring Data JPA for database interactions.
Spring Cache with Redis for caching.
H2 database driver for development.


Build Tool: Maven for dependency management and build automation.

5. API Example
5.1 Sample Request
{
  "transactionId": "txn-12345",
  "amount": 500.00,
  "ipAddress": "10.0.0.1",
  "originatorDetails": "name: John Doe",
  "transferDetails": "recipient: Jane Smith"
}

5.2 Sample Response (Approved)
{
  "transactionId": "txn-12345",
  "status": "APPROVED",
  "reason": "Transaction approved"
}

5.3 Sample Response (Rejected - Blocked IP)
{
  "transactionId": "txn-12348",
  "status": "REJECTED",
  "reason": "Transaction originated from blocked IP range (192.0.0.0 - 192.0.0.255)"
}

6. Testing Requirements

The system shall include a Postman collection to test the following scenarios:
Approved transaction (amount < $1,000).
Transaction on hold (amount between $1,000 and $2,000).
Rejected transaction (amount ≥ $2,000).
Rejected transaction (blocked IP in range 192.0.0.0–192.0.0.255).
Transaction with missing IP address (should proceed if amount-based rules allow).
Invalid request (missing required fields, e.g., transactionId).
Negative amount (should be rejected).


Unit tests shall cover all fraud rules and edge cases.
Load tests shall verify the system's ability to handle 10 million transactions per day.

7. Future Considerations

The system shall be designed to integrate additional fraud detection rules (e.g., machine learning-based rules, behavioral analysis).
Support for distributed caching and database clustering shall be considered for production deployment.
Monitoring and logging mechanisms shall be implemented to track transaction processing and rule evaluation performance.

8. Assumptions

The system assumes that the X-client-ip header is provided by a trusted upstream proxy or load balancer if used.
The initial implementation uses H2 for simplicity, but production deployment will require a persistent database.
The system assumes a single instance of Redis for caching, with potential for a Redis cluster in production.

9. Constraints

The system must handle high transaction volumes without significant performance degradation.
The initial rule set is limited to IP-based and amount-based rules, but the architecture must support expansion to 30,000–40,000 rules.
The API must be stateless to support horizontal scaling.

10. Deliverables

Spring Boot microservice source code.
Postman collection for API testing.
Documentation for API usage and deployment.
Unit and integration test suites.
Configuration for Redis and H2 database.
