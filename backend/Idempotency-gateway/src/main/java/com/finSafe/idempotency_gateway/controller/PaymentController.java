package com.finSafe.idempotency_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> processPayment(
            @RequestBody PaymentRequestDto requestBody,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
                
        log.info("Received payment request with Idempotency-Key: {}", idempotencyKey);
        try {
            String response = paymentService.processPayment(requestBody, idempotencyKey);
            log.info("Payment processed successfully for key: {}", idempotencyKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing payment for key: {}. Error: {}", idempotencyKey, e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing payment: " + e.getMessage());
        }

    }
}
