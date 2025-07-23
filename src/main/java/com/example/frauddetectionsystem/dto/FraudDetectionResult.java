package com.example.frauddetectionsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a fraud detection check for a transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResult {
    /**
     * The unique identifier of the transaction.
     */
    private String transactionId;
    
    /**
     * The status of the transaction after fraud detection.
     */
    private TransactionStatus status;
    
    /**
     * The reason for the status, providing details about the fraud check result.
     */
    private String reason;
    
    /**
     * Alias for getReason() to maintain backward compatibility with tests.
     * @return The reason for the fraud detection result.
     */
    public String getMessage() {
        return reason;
    }
}
