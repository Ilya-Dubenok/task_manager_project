package org.example.config.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


@ConditionalOnProperty(value = "needKafka", havingValue = "false", matchIfMissing = true)
@Configuration
public class KafkaEmptyConfigTest {




}
