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
            IdopRecord existingRecord = idopStore.getRecord(idempotencyKey);

            if (existingRecord != null) {

                // verifying the payload hash 
                if (existingRecord.getRequestBodyHash().equals(record.getRequestBodyHash())) {
                    log.info("Duplicate request with Idempotency-Key: {}", idempotencyKey);
                    return "Idempotency-Key already processed";
                } else {
                    log.error("Duplicate Idempotency-Key with different payload for key: {}", idempotencyKey);
                    return "Duplicate Idempotency-Key with different payload";
                }
                
            }
            log.info("Idempotency-Key does not exist. Saving new record: {}", idempotencyKey);
            idopStore.save(idempotencyKey, record);
            
        } catch (Exception e) {
            log.error("Error processing payment for key: {}. Error: {}", idempotencyKey, e.getMessage());
            return "Error processing payment: " + e.getMessage();
        }
            return "Payment processed successfully";

    }
}
