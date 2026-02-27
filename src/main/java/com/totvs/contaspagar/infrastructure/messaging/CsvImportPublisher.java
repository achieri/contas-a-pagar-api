package com.totvs.contaspagar.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CsvImportPublisher {

    private static final Logger log = LoggerFactory.getLogger(CsvImportPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CsvImportPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(CsvImportMessage message) {
        log.info("Publicando mensagem de importação CSV. Protocolo: {}", message.protocoloId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_CSV,
                message
        );
    }
}
