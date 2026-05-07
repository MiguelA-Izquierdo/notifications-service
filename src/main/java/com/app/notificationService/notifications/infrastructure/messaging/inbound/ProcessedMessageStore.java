package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessedMessageStore {

    private static final Logger logger = LoggerFactory.getLogger(ProcessedMessageStore.class);

    private record StoreEntry(long expiry, String contentHash) {}

    private final ConcurrentHashMap<String, StoreEntry> seen = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final int maxSize;

    public ProcessedMessageStore(
            @Value("${messaging.idempotency.ttl-hours:24}") long ttlHours,
            @Value("${messaging.idempotency.max-size:100000}") int maxSize) {
        this.ttlMillis = TimeUnit.HOURS.toMillis(ttlHours);
        this.maxSize = maxSize;
    }

    public boolean isAlreadyProcessed(String eventId, String contentHash) {
        StoreEntry entry = seen.get(eventId);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiry()) {
            seen.remove(eventId);
            return false;
        }
        if (!entry.contentHash().equals(contentHash)) {
            logger.warn("eventId collision detected (eventId={}): content hash differs. Processing message.", eventId);
            return false;
        }
        return true;
    }

    public void markAsProcessed(String eventId, String contentHash) {
        if (seen.size() >= maxSize) {
            long now = System.currentTimeMillis();
            seen.entrySet().removeIf(e -> now > e.getValue().expiry());
            if (seen.size() >= maxSize) {
                seen.entrySet().stream()
                        .min(Comparator.comparingLong(e -> e.getValue().expiry()))
                        .ifPresent(oldest -> {
                            logger.warn("Idempotency store at capacity ({}): evicting valid entry before TTL expiry (eventId={}). Raise messaging.idempotency.max-size if this recurs.", maxSize, oldest.getKey());
                            seen.remove(oldest.getKey());
                        });
            }
        }
        seen.put(eventId, new StoreEntry(System.currentTimeMillis() + ttlMillis, contentHash));
    }

    @Scheduled(fixedDelayString = "${messaging.idempotency.cleanup-interval-ms:3600000}")
    public void cleanup() {
        long now = System.currentTimeMillis();
        int before = seen.size();
        seen.entrySet().removeIf(e -> now > e.getValue().expiry());
        logger.debug("Idempotency store cleanup: {} entries removed, {} remaining", before - seen.size(), seen.size());
    }
}