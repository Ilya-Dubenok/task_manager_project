package org.example.service;


import org.example.core.dto.AuditCreateDTO;
import org.example.dao.entities.audit.Type;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.utils.config.ConsumerConf;
import org.example.utils.config.KafkaListenerWrapper;
import org.example.utils.config.Producer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
@Import({ConsumerConf.class, Producer.class})
public class KafkaListenerTest {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KafkaListenerWrapper kafkaListenerWrapper;




    @Test
    public void test1() throws InterruptedException {


        User user = new User(
                UUID.randomUUID(), "ijdifj@mail.ru", "someFio", UserRole.USER
        );
        AuditCreateDTO auditCreateDTO = new AuditCreateDTO(
                user, "Some text", Type.USER, "some id"
        );
        kafkaTemplate.send("transaction-1", auditCreateDTO);
        kafkaListenerWrapper.getLatch().await(10, TimeUnit.SECONDS);

    }




}
