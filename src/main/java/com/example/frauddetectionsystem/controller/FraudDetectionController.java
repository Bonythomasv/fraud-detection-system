package com.example.frauddetectionsystem.controller;

import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.dto.FraudDetectionResult;
import com.example.frauddetectionsystem.dto.request.TransactionRequest;
import com.example.frauddetectionsystem.service.FraudDetectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fraud-check")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    @Autowired
    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping
    public ResponseEntity<FraudDetectionResult> checkFraud(
            @Valid @RequestBody TransactionRequest request) {
        
        Transaction transaction = Transaction.fromRequest(request);
        FraudDetectionResult result = fraudDetectionService.checkTransaction(transaction);
        return ResponseEntity.ok(result);
    }
}
