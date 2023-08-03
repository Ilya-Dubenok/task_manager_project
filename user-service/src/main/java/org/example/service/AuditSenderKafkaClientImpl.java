package org.example.service;

import org.example.service.api.IAuditSenderKafkaClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AuditSenderKafkaClientImpl implements IAuditSenderKafkaClient<String, Object> {

    private KafkaTemplate<String, Object> kafkaTemplate;

    public AuditSenderKafkaClientImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> send(String topic, Object data) {
        return kafkaTemplate.send(topic, data);
    }
}
