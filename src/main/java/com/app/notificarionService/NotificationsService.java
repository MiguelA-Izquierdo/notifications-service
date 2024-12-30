package com.app.notificarionService;

import com.app.notificarionService.notifications.infrastructure.messaging.inbound.UserCreatedEventListener;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Objects;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
public class NotificationsService {
  private static final Logger logger = LoggerFactory.getLogger(NotificationsService.class);
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().load();
    System.setProperty("RABBITMQ_USER_SERVICE_HOST", Objects.requireNonNull(dotenv.get("RABBITMQ_USER_SERVICE_HOST")));
    System.setProperty("RABBITMQ_USER_SERVICE_PORT", Objects.requireNonNull(dotenv.get("RABBITMQ_USER_SERVICE_PORT")));
    System.setProperty("RABBITMQ_USER_SERVICE_USER_NAME", Objects.requireNonNull(dotenv.get("RABBITMQ_USER_SERVICE_USER_NAME")));
    System.setProperty("RABBITMQ_USER_SERVICE_PASSWORD", Objects.requireNonNull(dotenv.get("RABBITMQ_USER_SERVICE_PASSWORD")));

    System.setProperty("GMAIL_HOST", Objects.requireNonNull(dotenv.get("GMAIL_HOST")));
    System.setProperty("GMAIL_USERNAME", Objects.requireNonNull(dotenv.get("GMAIL_USERNAME")));
    System.setProperty("GMAIL_PASSWORD", Objects.requireNonNull(dotenv.get("GMAIL_PASSWORD")));


    SpringApplication.run(NotificationsService.class, args);
  }
}
