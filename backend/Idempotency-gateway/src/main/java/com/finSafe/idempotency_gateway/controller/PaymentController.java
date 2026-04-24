package com.finSafe.idempotency_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finSafe.idempotency_gateway.Dto.PaymentRequestDto;
import com.finSafe.idempotency_gateway.Services.PaymentService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process-payment")
    public ResponseEntity<String> processPayment(
            @RequestBody PaymentRequestDto requestBody,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
                
        // Fail fast if the header is completely missing
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Idempotency-Key header");
        }

        log.info("Received payment request with Idempotency-Key: {}", idempotencyKey);
        
        try {
            return paymentService.processPayment(requestBody, idempotencyKey);
        } catch (Exception e) {
            log.error("Error processing payment for key: {}. Error: {}", idempotencyKey, e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing payment: " + e.getMessage());
        }
    }
}