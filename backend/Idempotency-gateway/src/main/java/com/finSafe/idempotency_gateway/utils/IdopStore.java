package com.finSafe.idempotency_gateway.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.finSafe.idempotency_gateway.Dto.IdopRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IdopStore {

    private final ConcurrentHashMap<String, IdopRecord> store = new ConcurrentHashMap<>();

    public void save(String idempotencyKey, IdopRecord record) {
        log.info("Saving record for Idempotency-Key: {}", idempotencyKey);
        record.setState(IdopRecord.Status.COMPLETED);
        store.put(idempotencyKey, record);
    }

    public IdopRecord getRecord(String idempotencyKey) {
        return store.get(idempotencyKey);
    }

    public IdopRecord lockKeyForProcessing(String idempotencyKey, IdopRecord record) {

        return store.putIfAbsent(idempotencyKey, record);
    }

    @Scheduled(fixedRate = 600000)
    public void cleanStalesKeys() {
        log.info("Running TTL Cache Eviction Sweeper...");

        long now = System.currentTimeMillis();
        long twelveHours = TimeUnit.HOURS.toMillis(12);

        store.entrySet().removeIf(entry -> {
            boolean isStale = (now - entry.getValue().getCreatedAt()) > twelveHours;
            if (isStale) {
                log.info("Evicting stale Idempotency-Key: {}", entry.getKey());
            }
            return isStale;
        });

    }
}
