package com.app.notificarionService;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Objects;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
public class NotificationsService {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().load();
    System.setProperty("RABBITMQ_HOST", Objects.requireNonNull(dotenv.get("RABBITMQ_HOST")));
    System.setProperty("RABBITMQ_PORT", Objects.requireNonNull(dotenv.get("RABBITMQ_PORT")));
    System.setProperty("RABBITMQ_USER_NAME", Objects.requireNonNull(dotenv.get("RABBITMQ_USER_NAME")));
    System.setProperty("RABBITMQ_PASSWORD", Objects.requireNonNull(dotenv.get("RABBITMQ_PASSWORD")));
    SpringApplication.run(NotificationsService.class, args);
  }
}
