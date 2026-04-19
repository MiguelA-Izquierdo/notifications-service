# Notification Service

An event-driven microservice that listens to events from a RabbitMQ broker and sends transactional email notifications. It can consume events from any service connected to the same broker. Built with **Spring Boot 3.4** and **Java 17**, following **Domain-Driven Design (DDD)** principles.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.0 |
| Spring AMQP (RabbitMQ) | (included in Boot) |
| Spring Mail | (included in Boot) |
| Thymeleaf | (included in Boot) |
| Jackson | 2.17.0 |
| java-dotenv | 5.2.2 |

---

## Architecture

The project is organized following DDD with a clear separation between domain, application, and infrastructure layers:

```
src/main/java/com/app/notificarionService/
├── _shared/                         # Cross-cutting concerns
│   └── domain/
│       └── bus/event/               # Event interface + EventHandler<T> interface
└── notifications/
    ├── application/
    │   └── events/                  # Event DTOs (UserCreatedEvent, UserDeletedEvent)
    │       └── handlers/            # Event handlers — build the EmailNotification and delegate to EmailService
    ├── domain/
    │   ├── exceptions/              # Domain exceptions (ValueObjectValidationException)
    │   ├── model/                   # Domain models (EmailNotification, User, etc.)
    │   ├── service/                 # EmailService interface
    │   └── valueObject/
    │       └── notification/        # Value objects: Email, SubjectEmail
    └── infrastructure/
        ├── messaging/
        │   ├── config/              # RabbitMQ connection and queue/exchange declarations
        │   └── inbound/             # BaseEventListener<T> + concrete @RabbitListener entry points
        ├── serialization/           # Jackson configuration
        └── service/                 # EmailService implementations (JavaMail, Log)
```

`src/main/resources/templates/` — Thymeleaf HTML email templates.

### Event processing

Each incoming RabbitMQ message flows through two layers before triggering an action:

1. **`BaseEventListener<T>`** (abstract, in `infrastructure/messaging/inbound/`) — handles the low-level plumbing: deserializes the raw message body, calls `process()`, and performs manual **ACK** on success or **NACK with requeue** on failure. All concrete listeners extend it.
2. **`EventHandler<T>`** (interface, in `_shared/bus/event/`) — defines the single `handle(T event)` contract. Concrete handlers (e.g. `UserCreatedEventHandler`) reconstruct the domain objects, build the appropriate `EmailNotification`, and call `EmailService`.

```
RabbitMQ message
  └─▶ ConcreteEventListener (extends BaseEventListener)
        ├─ deserialize()  →  typed Event DTO
        └─ process()
              └─▶ ConcreteEventHandler (implements EventHandler)
                    └─▶ EmailService.sendEmail(EmailNotification)
```

Adding support for a new event only requires a new listener + handler pair; `BaseEventListener` and `EmailService` stay untouched.

### Email Notification model

`EmailNotification` is an **abstract class** that holds the common structure of any email (recipients, subject, HTML body). Each notification type extends it and implements the `generateHtml()` method with its own content:

```
EmailNotification  (abstract)
├── UserCreatedEmailNotification   →  "Bienvenido {name}" — welcome email
└── UserDeletedEmailNotification   →  "Hasta pronto {name}" — account deletion email
```

Adding a new notification type only requires creating a new subclass and implementing `generateHtml()`. The sending logic in `EmailService` stays untouched.

---

## Integration with Other Services

The Notification Service is a **downstream consumer**: it connects to a RabbitMQ broker and subscribes to queues bound to the exchanges declared by other services. No direct HTTP calls are made between services.

```
Any Service  ──(publishes)──▶  exchange (topic)  ──(routes)──▶  Notification Service
```

Currently it handles events from the **`userExchange`** exchange, declared by the [User Service](../user-service). Adding support for a new source only requires declaring a new listener and its corresponding queue/binding configuration.

---

## Events Consumed

All events are received from the **`userExchange`** exchange (topic type).

| Event | Routing key | Queue | Trigger | Email sent |
|---|---|---|---|---|
| `UserCreatedEvent` | `user.created` | `userCreatedQueue` | User registered in User Service | Welcome email |
| `UserDeletedEvent` | `user.deleted` | `userDeletedQueue` | User deleted in User Service | Account deletion confirmation |

> **Note:** `userUpdatedQueue` (`user.updated`) is declared and bound but has no handler yet — it is reserved for future use.

---

## Email Service Implementations

There are two implementations of `EmailService`:

| Implementation | Active | Behaviour |
|---|---|---|
| `EmailServiceJavaMailImplement` | Production (`@Primary`) | Sends real emails via SMTP using JavaMailSender + Thymeleaf |
| `EmailServiceLogImplement` | Development fallback | Logs the email content to the console instead of sending |

Emails are sent asynchronously. The HTML body is rendered from `src/main/resources/templates/email-template.html` using Thymeleaf before delivery.

---

## Message Reliability

Messages are acknowledged manually (`ackMode = "MANUAL"`). `BaseEventListener` sends a **basicAck** when processing succeeds and a **basicNack with requeue=true** when it throws, so no message is silently dropped.

When processing fails, RabbitMQ redelivers the message automatically because the nack is issued with `requeue = true`. Retries are broker-driven: the message re-enters the queue until it succeeds or is manually moved. Persistent failures route to the Dead Letter Queue (DLQ) configured per queue.

---

## Deployment

| Mode | Prerequisites | Guide |
|---|---|---|
| Docker Compose | Docker | [docs/deployment.md → Docker Compose](docs/deployment.md#docker-compose-recommended) |
| Local (no Docker) | Java 17, Maven, RabbitMQ | [docs/deployment.md → Local development](docs/deployment.md#local-development-without-docker) |

**Docker Compose quick-start:**

```bash
cp src/main/resources/.env.example .env.docker   # fill in secrets
docker compose --env-file .env.docker up --build
```

**Local quick-start:**

```bash
cp src/main/resources/.env.example .env          # fill in local values
./mvnw spring-boot:run
```

See **[docs/deployment.md](docs/deployment.md)** for the full guide: environment variable reference and RabbitMQ connection details.