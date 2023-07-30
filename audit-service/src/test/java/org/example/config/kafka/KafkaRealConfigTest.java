package org.example.config.kafka;


import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.core.dto.AuditCreateDTO;
import org.example.endpoint.kafka.KafkaListenerEndpoint;
import org.example.service.api.IAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(value = "needKafka", havingValue = "true", matchIfMissing = false)
@Configuration
public class KafkaRealConfigTest {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;


    @Bean
    public ConsumerFactory<String, AuditCreateDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID
        );

        props.put(
                JsonDeserializer.USE_TYPE_INFO_HEADERS, "false"
        );
        props.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE, "org.example.core.dto.AuditCreateDTO"
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(AuditCreateDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditCreateDTO>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, AuditCreateDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public KafkaListenerEndpoint kafkaListenerEndpoint(@Autowired IAuditService auditService) {

        return new KafkaListenerEndpoint(auditService);
    }

    @ConditionalOnProperty(value = "needKafka", havingValue = "true", matchIfMissing = false)
    @Configuration
    public static class KafkaProducerConfiguration {
        @Bean
        public Map<String, Object> producerConfiguration() {

            Map<String, Object> properties = new HashMap<>();

            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return properties;

        }


        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            return new DefaultKafkaProducerFactory<>(producerConfiguration());

        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public NewTopic topic() {

            return new NewTopic("audit_info", 1, (short) 1);
        }

    }

}
