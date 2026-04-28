package com.app.notificationService.notifications.infrastructure.messaging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
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

    @Value("${messaging.exchange.dlx}")
    private String dlxExchange;

    @Value("${messaging.queue.userCreated}")
    private String userCreatedQueue;

    @Value("${messaging.queue.userUpdated}")
    private String userUpdatedQueue;

    @Value("${messaging.queue.userDeleted}")
    private String userDeletedQueue;

    @Value("${messaging.queue.userCreated.dlq}")
    private String userCreatedDlq;

    @Value("${messaging.queue.userUpdated.dlq}")
    private String userUpdatedDlq;

    @Value("${messaging.queue.userDeleted.dlq}")
    private String userDeletedDlq;

    @Value("${messaging.routing.key.userCreated}")
    private String userCreatedRoutingKey;

    @Value("${messaging.routing.key.userUpdated}")
    private String userUpdatedRoutingKey;

    @Value("${messaging.routing.key.userDeleted}")
    private String userDeletedRoutingKey;

    // --- Main exchange ---

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(userExchange);
    }

    // --- Dead Letter Exchange ---

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchange);
    }

    // --- Main queues (with DLX routing) ---

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", userCreatedDlq)
                .build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(userUpdatedQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", userUpdatedDlq)
                .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(userDeletedQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", userDeletedDlq)
                .build();
    }

    // --- Dead Letter Queues ---

    @Bean
    public Queue userCreatedDlqQueue() {
        return new Queue(userCreatedDlq, true);
    }

    @Bean
    public Queue userUpdatedDlqQueue() {
        return new Queue(userUpdatedDlq, true);
    }

    @Bean
    public Queue userDeletedDlqQueue() {
        return new Queue(userDeletedDlq, true);
    }

    // --- Main queue bindings ---

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

    // --- DLQ bindings ---

    @Bean
    public Binding userCreatedDlqBinding() {
        return BindingBuilder.bind(userCreatedDlqQueue()).to(deadLetterExchange()).with(userCreatedDlq);
    }

    @Bean
    public Binding userUpdatedDlqBinding() {
        return BindingBuilder.bind(userUpdatedDlqQueue()).to(deadLetterExchange()).with(userUpdatedDlq);
    }

    @Bean
    public Binding userDeletedDlqBinding() {
        return BindingBuilder.bind(userDeletedDlqQueue()).to(deadLetterExchange()).with(userDeletedDlq);
    }
}