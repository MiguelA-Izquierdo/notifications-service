# Deployment

## Docker Compose (recommended)

The fastest way to run the service. The RabbitMQ broker is **not** included — it is assumed to be running elsewhere (e.g. started by the User Service stack or any other shared instance).

**1. Create your environment file:**

```bash
cp src/main/resources/.env.example .env.docker
```

Edit `.env.docker` with your RabbitMQ connection details and Gmail credentials.

**2. Start the service:**

```bash
docker compose --env-file .env.docker up --build
```

This starts one container:

| Container | Description | Host port → Container port |
|---|---|---|
| `notifications-service` | Spring Boot application | `8081 → 8081` |

> `.env.docker` is listed in `.gitignore` and will never be committed.

---

## Local development (without Docker)

**Prerequisites:** Java 17, Maven, a running RabbitMQ instance, a Gmail account with an [App Password](https://support.google.com/accounts/answer/185833).

**1. Create your environment file:**

```bash
cp src/main/resources/.env.example .env
```

Edit `.env` with your local RabbitMQ and Gmail credentials.

**2. Run the application:**

```bash
./mvnw spring-boot:run
```

The service starts at `http://localhost:8081`.

---

## Environment variables reference

| Variable | Description | Required |
|---|---|---|
| `RABBITMQ_HOST` | RabbitMQ host | Yes |
| `RABBITMQ_PORT` | RabbitMQ port (default `5672`) | Yes |
| `RABBITMQ_USERNAME` | RabbitMQ username | Yes |
| `RABBITMQ_PASSWORD` | RabbitMQ password | Yes |
| `GMAIL_HOST` | SMTP host (e.g. `smtp.gmail.com`) | Yes |
| `GMAIL_USERNAME` | SMTP username / sender address | Yes |
| `GMAIL_PASSWORD` | SMTP password or App Password | Yes |

> **Note:** The RabbitMQ virtual host is hardcoded to `/` in `application.properties`. If your broker uses a different virtual host, edit that property directly before building.