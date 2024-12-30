  package com.app.notificarionService.notifications.infrastructure.messaging.config;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.amqp.core.Binding;
  import org.springframework.amqp.core.Queue;
  import org.springframework.amqp.core.TopicExchange;
  import org.springframework.amqp.rabbit.annotation.EnableRabbit;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;

  @Configuration
  @EnableRabbit
  public class UserRabbitMqConfig {
    private static final Logger logger = LoggerFactory.getLogger(UserRabbitMqConfig.class);

    @Value("${messaging.exchange.user}")
    private String userExchange;

    @Value("${messaging.queue.userCreated}")
    private String userCreatedQueue;

    @Value("${messaging.queue.userUpdated}")
    private String userUpdatedQueue;

    @Value("${messaging.queue.userDeleted}")
    private String userDeletedQueue;

    @Value("${messaging.routing.key.userCreated}")
    private String userCreatedRoutingKey;

    @Value("${messaging.routing.key.userUpdated}")
    private String userUpdatedRoutingKey;

    @Value("${messaging.routing.key.userDeleted}")
    private String userDeletedRoutingKey;

    @Bean
    public Queue userCreatedQueue() {
      return new Queue(userCreatedQueue, true);
    }

    @Bean
    public Queue userUpdatedQueue() {
      return new Queue(userUpdatedQueue, true);
    }

    @Bean
    public Queue userDeletedQueue() {
      return new Queue(userDeletedQueue, true);
    }


    @Bean
    public TopicExchange userExchange() {
      return new TopicExchange(userExchange);
    }

    @Bean
    public Binding userCreatedBinding() {
      return new Binding(userCreatedQueue, Binding.DestinationType.QUEUE,
        userExchange, userCreatedRoutingKey, null);
    }

    @Bean
    public Binding userUpdatedBinding() {
      return new Binding(userUpdatedQueue, Binding.DestinationType.QUEUE,
        userExchange, userUpdatedRoutingKey, null);
    }

    @Bean
    public Binding userDeletedBinding() {
      return new Binding(userDeletedQueue, Binding.DestinationType.QUEUE,
        userExchange, userDeletedRoutingKey, null);
    }
  }
