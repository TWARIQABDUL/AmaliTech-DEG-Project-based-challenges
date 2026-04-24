package com.finSafe.idempotency_gateway.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finSafe.idempotency_gateway.Dto.IdopRecord;
import com.finSafe.idempotency_gateway.Dto.PaymentRequestDto;
import com.finSafe.idempotency_gateway.utils.IdopStore;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private IdopStore idopStore;

    public ResponseEntity<String> processPayment(PaymentRequestDto requestBody, String idempotencyKey) {
        String successMessage = "Charged " + requestBody.getAmount() + " " + requestBody.getCurrency();
        IdopRecord record = new IdopRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestBodyHash(requestBody.toString());
        record.setState(IdopRecord.Status.PROCESSING);
        record.setResponseStatus(200);
        record.setResponseBody(successMessage);

        log.info("Processing payment for Idempotency-Key: {}", idempotencyKey);

        try {
            // IdopRecord existingRecord = idopStore.getRecord(idempotencyKey);
            IdopRecord existingRecord = idopStore.lockKeyForProcessing(idempotencyKey, record);

            if (existingRecord != null) {

                // verifying the payload hash
                if (existingRecord.getRequestBodyHash().equals(record.getRequestBodyHash())) {
                    
                    log.info("Duplicate request with Idempotency-Key: {}", idempotencyKey);
                    if (existingRecord.getState() == IdopRecord.Status.PROCESSING) {
                        log.info("Duplicate request is being processed");
                        while (existingRecord.getState() == IdopRecord.Status.PROCESSING) {
                            log.info("Waiting for payment to be processed");
                            TimeUnit.SECONDS.sleep(1);
                            existingRecord = idopStore.getRecord(idempotencyKey);
                            if (existingRecord == null) {
                                log.error("Idempotency-Key not found");
                                return ResponseEntity.badRequest().body("Idempotency-Key not found");
                            }
                        }
                        log.info("Payment processed successfully for key: {}", idempotencyKey);
                        return ResponseEntity.ok()
                                .header("X-Cache-Hit", "true")
                                .body(existingRecord.getResponseBody());
                        // return ResponseEntity.ok(existingRecord.getResponseBody());
                    }
                    log.info("Payment processed successfully for key: {}", idempotencyKey);
                    return ResponseEntity.ok()
                            .header("X-Cache-Hit", "true")
                            .body(existingRecord.getResponseBody());
                    // return ResponseEntity.ok(existingRecord.getResponseBody());
                } else {
                    log.error("Duplicate Idempotency-Key with different payload for key: {}", idempotencyKey);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Idempotency key already used for a different request body.");
                    // return ResponseEntity.badRequest().body("Duplicate Idempotency-Key with
                    // different payload");
                }

            }
            try {
                log.info("Idempotency-Key does not exist. Saving new record: {}", idempotencyKey);
                log.info("Waiting for 2 seconds for payment processing");
                TimeUnit.SECONDS.sleep(2);
                idopStore.save(idempotencyKey, record);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // log.info("Idempotency-Key does not exist. Saving new record: {}",
            // idempotencyKey);
            // idopStore.save(idempotencyKey, record);

        } catch (Exception e) {
            log.error("Error processing payment for key: {}. Error: {}", idempotencyKey, e.getMessage());
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
        return ResponseEntity.ok(successMessage);

    }
}
