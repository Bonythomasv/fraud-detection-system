package com.example.frauddetectionsystem.controller;

import com.example.frauddetectionsystem.domain.Transaction;
import com.example.frauddetectionsystem.domain.TransactionDetails;
import com.example.frauddetectionsystem.dto.FraudDetectionResult;
import com.example.frauddetectionsystem.dto.TransactionStatus;
import com.example.frauddetectionsystem.dto.request.TransactionRequest;
import com.example.frauddetectionsystem.service.FraudDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class FraudDetectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @InjectMocks
    private FraudDetectionController fraudDetectionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fraudDetectionController).build();
    }

    @Test
    void testCheckFraud_Approved() throws Exception {
        // Arrange
        TransactionRequest request = createTestRequest("500", "192.168.1.1");
        FraudDetectionResult expectedResult = new FraudDetectionResult(
            "tx123", 
            TransactionStatus.APPROVED, 
            "Approved"
        );

        when(fraudDetectionService.checkTransaction(any(Transaction.class))).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/v1/fraud-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("tx123"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reason").value("Approved"));
    }

    private TransactionRequest createTestRequest(String amount, String ipAddress) {
        TransactionRequest request = new TransactionRequest();
        request.setTransactionId("tx123");
        request.setAmount(new BigDecimal(amount));
        request.setIpAddress(ipAddress);
        
        Map<String, String> originatorDetails = new HashMap<>();
        originatorDetails.put("name", "Test User");
        originatorDetails.put("account", "123456");
        
        Map<String, String> eTransferDetails = new HashMap<>();
        eTransferDetails.put("recipient", "Recipient User");
        eTransferDetails.put("recipientAccount", "654321");
        
        TransactionDetails originator = new TransactionDetails();
        originator.setDetails(originatorDetails);
        
        TransactionDetails eTransfer = new TransactionDetails();
        eTransfer.setDetails(eTransferDetails);
        
        request.setOriginatorDetails(originator);
        request.setETransferDetails(eTransfer);
        
        return request;
    }
}
