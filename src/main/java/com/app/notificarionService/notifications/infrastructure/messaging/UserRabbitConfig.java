package com.app.notificarionService.notifications.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class UserRabbitConfig {

  @Value("${messaging.exchange.user}")
  private String userExchange;

  @Value("${messaging.queue.user.created}")
  private String userCreatedQueue;

  @Value("${messaging.queue.user.updated}")
  private String userUpdatedQueue;

  @Value("${messaging.queue.user.deleted}")
  private String userDeletedQueue;

  @Value("${messaging.routing.key.user.created}")
  private String userCreatedRoutingKey;

  @Value("${messaging.routing.key.user.updated}")
  private String userUpdatedRoutingKey;

  @Value("${messaging.routing.key.user.deleted}")
  private String userDeletedRoutingKey;

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
    return rabbitTemplate;
  }
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
