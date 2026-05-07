# Messaging Guarantees

## Delivery semantics

RabbitMQ guarantees **at-least-once delivery** — not exactly-once. A message can be delivered more than once if:

- The consumer processes the message but the connection drops before the ACK reaches the broker → the broker considers it unacknowledged and redelivers it to the next available consumer.
- The service restarts with in-flight (unacknowledged) messages → RabbitMQ requeues them automatically.
- A transient broker failure causes a connection reset mid-flight.

This service handles that reality at two levels: a **retry mechanism** for transient processing failures, and an **idempotency store** to deduplicate legitimate redeliveries.

---

## Retry mechanism

When message processing fails (e.g. SMTP timeout), the message is not immediately dead-lettered. Instead it is re-published to a retry exchange with a TTL-based delay before re-entering the main queue:

| Attempt | Delay queue | Wait time |
|---|---|---|
| 1st retry | `<queue>.retry.30s` | 30 seconds |
| 2nd retry | `<queue>.retry.2min` | 2 minutes |
| 3rd retry | `<queue>.retry.10min` | 10 minutes |
| Exhausted | `<queue>.parking` | Manual review |

The retry count is tracked in the `x-retry-count` header. Once all retries are exhausted the message lands in the **parking lot queue** (`<queue>.parking`) where it can be inspected or replayed manually.

Deserialization failures (malformed JSON, wrong schema) skip the retry path entirely and go straight to the DLQ — retrying a message that cannot be parsed would never succeed.

---

## Idempotency

Because RabbitMQ delivers at-least-once, the same message can arrive more than once. Without a deduplication layer, each redelivery would trigger a duplicate email.

### How it works

Each event published by upstream services must include an `eventId` field at the **envelope level** (not inside the domain payload):

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "exchange": "userExchange",
  "routingKey": "user.created",
  "payload": "{\"userId\":\"...\",\"name\":\"...\"}"
}
```

On every message the listener reads `eventId` from the envelope. Before processing, it checks whether that ID has already been handled. If yes, the message is ACKed and discarded silently. If no, processing proceeds normally and the ID is recorded after a successful ACK.

The check happens **after deserialization and before processing**, and the mark happens **after the ACK** — this ensures that if the service crashes between processing and ACKing, the next redelivery will be processed again (safe) rather than silently skipped (data loss).

If a message arrives without `eventId` the deduplication check is skipped and the message is processed normally. This is intentional degradation: it preserves backwards compatibility with publishers that have not yet been updated.

### Current implementation: in-memory store

`ProcessedMessageStore` uses a `ConcurrentHashMap` keyed by `eventId`, storing both the expiry timestamp and a SHA-256 content hash (`routingKey + body`). The content hash guards against `eventId` reuse: if the same ID arrives with different content (buggy or malicious producer), the message is processed again and a `WARN` is logged. Entries are cleaned up hourly by a scheduled task.

**Capacity:** configurable via `messaging.idempotency.max-size` (default 100 000, ≈ 40 MB). When the cap is reached the store first purges already-expired entries. If still full, it evicts the oldest valid entry and logs:

```
WARN — Idempotency store at capacity (100000): evicting valid entry before TTL expiry (eventId=…). Raise messaging.idempotency.max-size if this recurs.
```

This log is the operational signal to increase the cap before the eviction starts causing duplicate processing.

#### Known limitations

| Scenario | Behaviour |
|---|---|
| Redelivery on the same instance | Duplicate detected correctly |
| `eventId` reused with different payload | Collision detected — message processed, `WARN` logged |
| Store full, all entries valid | Oldest entry evicted before TTL expiry — `WARN` logged |
| Redelivery after service restart | Store is lost — duplicate **not** detected |
| Redelivery on a different instance (horizontal scaling) | Stores are independent — duplicate **not** detected |

These limitations are acceptable for a **single-instance deployment** with low restart frequency. The retry window is at most ~13 minutes (30s + 2min + 10min), so the practical duplicate risk on a running instance is well-contained.

### Migrating to Redis (recommended for production)

For multi-instance deployments or environments where the service restarts frequently, replace `ProcessedMessageStore` with a Redis-backed implementation. The public interface (`isAlreadyProcessed`, `markAsProcessed`) does not change — only the implementation needs to swap `ConcurrentHashMap` for a `StringRedisTemplate` call:

```java
// SET eventId "1" NX EX 86400
// Returns true  → first time seen (process the message)
// Returns false → already processed (skip)
Boolean isNew = redisTemplate.opsForValue()
        .setIfAbsent(eventId, "1", Duration.ofHours(24));
```

The `SET NX EX` command is atomic — no race conditions under concurrent consumers. Required additions:

- `spring-boot-starter-data-redis` dependency in `pom.xml`
- `REDIS_HOST` / `REDIS_PORT` environment variables
- A Redis service in `docker-compose.yml`

---

## Notification coverage

| Event | Notification |
|---|---|
| `user.created` | Welcome email |
| `user.deleted` | Account removal email |
| `user.updated` | None — notification strategy not defined yet |

---

## Event contract

Publishers must set `eventId` as a UUID string at the root of the message envelope. The field is optional in the current implementation (missing `eventId` degrades to no-dedup), but **strongly recommended** for all new event types to ensure idempotency.

The domain payload (`payload` field) must not include `eventId` — it belongs to the event envelope, not to the domain data.