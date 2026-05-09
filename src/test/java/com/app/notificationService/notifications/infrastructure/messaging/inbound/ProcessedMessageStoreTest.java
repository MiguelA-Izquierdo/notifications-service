package com.app.notificationService.notifications.infrastructure.messaging.inbound;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProcessedMessageStoreTest {

    @Test
    void shouldReturnFalseForUnknownEventId() {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 1000);

        assertThat(store.isAlreadyProcessed("unknown-id", "hash")).isFalse();
    }

    @Test
    void shouldReturnTrueForAlreadyProcessedEvent() {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 1000);
        store.markAsProcessed("event-1", "hash-abc");

        assertThat(store.isAlreadyProcessed("event-1", "hash-abc")).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSameIdArrivesWithDifferentContentHash() {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 1000);
        store.markAsProcessed("event-1", "hash-v1");

        assertThat(store.isAlreadyProcessed("event-1", "hash-v2")).isFalse();
    }

    @Test
    void shouldReturnFalseAfterEntryTtlExpires() throws InterruptedException {
        ProcessedMessageStore store = new ProcessedMessageStore(0L, 1000);
        store.markAsProcessed("event-1", "hash-1");
        Thread.sleep(1);

        assertThat(store.isAlreadyProcessed("event-1", "hash-1")).isFalse();
    }

    @Test
    void shouldEvictOldestEntryWhenStoreIsFullAndNoEntriesHaveExpired() throws InterruptedException {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 2);
        store.markAsProcessed("event-1", "hash-1");
        Thread.sleep(1);
        store.markAsProcessed("event-2", "hash-2");

        store.markAsProcessed("event-3", "hash-3");

        assertThat(store.isAlreadyProcessed("event-1", "hash-1")).isFalse();
        assertThat(store.isAlreadyProcessed("event-2", "hash-2")).isTrue();
        assertThat(store.isAlreadyProcessed("event-3", "hash-3")).isTrue();
    }

    @Test
    void shouldPurgeExpiredEntriesBeforeEvictingValidOnes() throws InterruptedException {
        ProcessedMessageStore store = new ProcessedMessageStore(0L, 2);
        store.markAsProcessed("event-expiring-1", "hash-1");
        store.markAsProcessed("event-expiring-2", "hash-2");
        Thread.sleep(1);

        store.markAsProcessed("event-fresh", "hash-fresh");

        assertThat(store.isAlreadyProcessed("event-fresh", "hash-fresh")).isTrue();
    }

    @Test
    void shouldRemoveExpiredEntriesOnCleanup() throws InterruptedException {
        ProcessedMessageStore store = new ProcessedMessageStore(0L, 1000);
        store.markAsProcessed("event-1", "hash-1");
        store.markAsProcessed("event-2", "hash-2");
        Thread.sleep(1);

        store.cleanup();

        assertThat(store.isAlreadyProcessed("event-1", "hash-1")).isFalse();
        assertThat(store.isAlreadyProcessed("event-2", "hash-2")).isFalse();
    }

    @Test
    void shouldNotThrowWhenCleanupRunsOnEmptyStore() {
        ProcessedMessageStore store = new ProcessedMessageStore(24L, 1000);

        assertThatCode(store::cleanup).doesNotThrowAnyException();
    }
}