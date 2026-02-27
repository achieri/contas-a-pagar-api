package com.totvs.contaspagar.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE        = "contas.exchange";
    public static final String QUEUE_CSV       = "contas.csv.import.queue";
    public static final String QUEUE_CSV_DLQ   = "contas.csv.import.dlq";
    public static final String ROUTING_CSV     = "contas.csv.import";
    public static final String ROUTING_CSV_DLQ = "contas.csv.import.dead";

    // --- Dead Letter Queue ---
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(EXCHANGE + ".dlx");
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(QUEUE_CSV_DLQ).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(dlqExchange()).with(ROUTING_CSV_DLQ);
    }

    // --- Main Exchange e Queue ---
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue csvImportQueue() {
        return QueueBuilder.durable(QUEUE_CSV)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", ROUTING_CSV_DLQ)
                .build();
    }

    @Bean
    public Binding csvImportBinding() {
        return BindingBuilder.bind(csvImportQueue()).to(exchange()).with(ROUTING_CSV);
    }

    // --- Serialização JSON ---
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
