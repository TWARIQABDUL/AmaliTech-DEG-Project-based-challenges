package com.finSafe.idempotency_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finSafe.idempotency_gateway.Dto.PaymentRequestDto;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequestDto requestBody) {
        return ResponseEntity.ok("Charged With" + requestBody.getAmount() + " " + requestBody.getCurrency());
    }
}
