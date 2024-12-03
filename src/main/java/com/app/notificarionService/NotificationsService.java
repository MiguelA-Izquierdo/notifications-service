package com.app.notificarionService;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
public class NotificationsService {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().load();


    SpringApplication.run(NotificationsService.class, args);
  }
}
