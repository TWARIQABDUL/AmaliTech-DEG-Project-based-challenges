package com.finSafe.idempotency_gateway.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finSafe.idempotency_gateway.Dto.IdopRecord;
import com.finSafe.idempotency_gateway.Dto.PaymentRequestDto;
import com.finSafe.idempotency_gateway.utils.IdopStore;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private IdopStore idopStore;

    public String processPayment(PaymentRequestDto requestBody, String idempotencyKey) {

        IdopRecord record = new IdopRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestBodyHash(requestBody.toString());
        record.setState(IdopRecord.Status.PROCESSING);
        record.setResponseStatus(200);
        record.setResponseBody("Payment processed successfully");

        log.info("Processing payment for Idempotency-Key: {}", idempotencyKey);
        
        try {
            log.info("Verifying Idempotency-Key: {}", idempotencyKey);
            if (idopStore.verifyIdempotencyKey(idempotencyKey, record)) {
                log.info("Idempotency-Key already exists: {}", idempotencyKey);
                log.warn("Duplicate request detected for Idempotency-Key: {}", idempotencyKey);
                return "Payment already processed";
            }
            log.info("Idempotency-Key does not exist: {}", idempotencyKey);
            log.info("Saving Idempotency-Key: {}", idempotencyKey);
            idopStore.save(idempotencyKey, record);

            
        } catch (Exception e) {
            // TODO: handle exception
        }
        return "Payment processed successfully";
    }
}
