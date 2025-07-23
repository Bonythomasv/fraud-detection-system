package com.example.frauddetectionsystem.domain;

import com.example.frauddetectionsystem.dto.TransactionStatus;
import com.example.frauddetectionsystem.dto.request.TransactionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String transactionId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(name = "status_reason", length = 500)
    private String statusReason;
    
    @Embedded
    private TransactionDetails originatorDetails;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "details", column = @Column(name = "e_transfer_details"))
    })
    private TransactionDetails eTransferDetails;
    
    // Alias for getter to match JSON property name
    public TransactionDetails getETransferDetails() {
        return eTransferDetails;
    }
    
    // Alias for setter to match JSON property name
    public void setETransferDetails(TransactionDetails eTransferDetails) {
        this.eTransferDetails = eTransferDetails;
    }
    
    public static Transaction fromRequest(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(request.getTransactionId());
        transaction.setAmount(request.getAmount());
        transaction.setIpAddress(request.getIpAddress());
        transaction.setOriginatorDetails(request.getOriginatorDetails());
        transaction.setETransferDetails(request.getETransferDetails());
        return transaction;
    }
}
