package com.finSafe.idempotency_gateway.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.finSafe.idempotency_gateway.Dto.IdopRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdopStore {

    private final ConcurrentHashMap<String, IdopRecord> store = new ConcurrentHashMap<>();

    public void save(String idempotencyKey, IdopRecord record) {
        log.info("Saving record for Idempotency-Key from: {}", idempotencyKey);
        record.setState(IdopRecord.Status.COMPLETED);
        store.put(idempotencyKey, record);
        log.info("Record saved for Idempotency-Key from: {}", idempotencyKey);
    }

    public boolean verifyIdempotencyKey(String idempotencyKey, IdopRecord record) {
        IdopRecord existingRecord = store.get(idempotencyKey);
log.info("Verifying Idempotency-Key status from: {}", record.getState());
        if (store.containsKey(idempotencyKey) && existingRecord.getRequestBodyHash().equals(record.getRequestBodyHash())) {

            log.debug("Idempotency-Key hit: {}", idempotencyKey);
            return true;
        }
       log.debug("Idempotency-Key miss or pending: {}", idempotencyKey);
       return false;
    }

}
