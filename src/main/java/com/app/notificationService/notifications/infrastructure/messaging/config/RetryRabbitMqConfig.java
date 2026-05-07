package com.app.notificationService.notifications.infrastructure.messaging.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RetryRabbitMqConfig {

    private static final int TTL_30S   =  30_000;
    private static final int TTL_2MIN  = 120_000;
    private static final int TTL_10MIN = 600_000;

    @Value("${messaging.exchange.user}")
    private String userExchange;

    @Value("${messaging.exchange.retry}")
    private String retryExchange;

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
    public DirectExchange retryExchange() {
        return new DirectExchange(retryExchange);
    }

    @Bean
    public Declarables retryDeclarables(DirectExchange retryExchange) {
        List<Declarable> all = new ArrayList<>();
        all.addAll(retrySetFor(userCreatedQueue, userCreatedRoutingKey, retryExchange));
        all.addAll(retrySetFor(userUpdatedQueue, userUpdatedRoutingKey, retryExchange));
        all.addAll(retrySetFor(userDeletedQueue, userDeletedRoutingKey, retryExchange));
        return new Declarables(all);
    }

    private List<Declarable> retrySetFor(String queueName, String originalRoutingKey, DirectExchange exchange) {
        List<Declarable> result = new ArrayList<>();

        String[] suffixes = {".retry.30s", ".retry.2min", ".retry.10min"};
        int[] ttls = {TTL_30S, TTL_2MIN, TTL_10MIN};

        for (int i = 0; i < suffixes.length; i++) {
            String name = queueName + suffixes[i];
            Queue queue = QueueBuilder.durable(name)
                    .withArgument("x-message-ttl", ttls[i])
                    .withArgument("x-dead-letter-exchange", userExchange)
                    .withArgument("x-dead-letter-routing-key", originalRoutingKey)
                    .build();
            result.add(queue);
            result.add(BindingBuilder.bind(queue).to(exchange).with(name));
        }

        String parkingName = queueName + ".parking";
        Queue parking = new Queue(parkingName, true);
        result.add(parking);
        result.add(BindingBuilder.bind(parking).to(exchange).with(parkingName));

        return result;
    }
}