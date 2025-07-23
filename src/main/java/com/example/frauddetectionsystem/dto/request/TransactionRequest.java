package com.example.frauddetectionsystem.dto.request;

import com.example.frauddetectionsystem.domain.TransactionDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.bind.Name;

public class TransactionRequest {
    @NotBlank(message = "Transaction ID is required and cannot be empty")
    private String transactionId;

    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    @Valid
    private TransactionDetails originatorDetails;

    @Valid
    private TransactionDetails eTransferDetails;

    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$", 
            message = "Invalid IP address format")
    private String ipAddress;

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionDetails getOriginatorDetails() {
        return originatorDetails;
    }

    public void setOriginatorDetails(TransactionDetails originatorDetails) {
        this.originatorDetails = originatorDetails;
    }

    public TransactionDetails getETransferDetails() {
        return eTransferDetails;
    }

    public void setETransferDetails(TransactionDetails eTransferDetails) {
        this.eTransferDetails = eTransferDetails;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
