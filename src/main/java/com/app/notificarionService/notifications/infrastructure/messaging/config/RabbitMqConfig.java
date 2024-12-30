package com.app.notificarionService.notifications.infrastructure.messaging.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMqConfig{
  private static final Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);


  @Value("${rabbitmq.userService.host}")
  private String userServiceHost;

  @Value("${rabbitmq.userService.port}")
  private int userServicePort;

  @Value("${rabbitmq.userService.username}")
  private String userServiceUsername;

  @Value("${rabbitmq.userService.password}")
  private String userServicePassword;

  @Value("${rabbitmq.userService.virtualHost}")
  private String userServiceVirtualHost;

  @Value("${rabbitmq.retry.maxAttempts}")
  private Integer maxAttempts;

  @Value("${rabbitmq.retry.initialInterval}")
  private Long initialInterval;
  @Value("${rabbitmq.retry.maxInterval}")
  private Long maxInterval;
  @Value("${rabbitmq.retry.multiplier}")
  private Float multiplier;

  @PostConstruct
  public void validateConfig() {
    if (userServiceHost == null || userServiceUsername == null || userServicePassword == null) {
      throw new IllegalArgumentException("RabbitMQ connection parameters are missing!");
    }
  }
  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setHost(userServiceHost);
    connectionFactory.setPort(userServicePort);
    connectionFactory.setUsername(userServiceUsername);
    connectionFactory.setPassword(userServicePassword);
    connectionFactory.setVirtualHost(userServiceVirtualHost);
    connectionFactory.setConnectionTimeout(30000);
    return connectionFactory;
  }

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory userConnectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(userConnectionFactory);
    rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
    rabbitTemplate.setRetryTemplate(retryTemplate());
    rabbitTemplate.setMandatory(true);
    rabbitTemplate.setChannelTransacted(true);
    return rabbitTemplate;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(initialInterval);
    backOffPolicy.setMultiplier(multiplier);
    backOffPolicy.setMaxInterval(maxInterval);
    retryTemplate.setBackOffPolicy(backOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(maxAttempts);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
