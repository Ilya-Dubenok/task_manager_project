package org.example.utils.config;

import org.example.core.dto.AuditCreateDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class KafkaListenerWrapper {

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload;

    @KafkaListener(
            topics = "transaction-1"
    )
    public void listener(AuditCreateDTO auditCreateDTO) {
        System.out.println("\n\n\n\n\nFUCKING YEAH!!!!!!!\n\n\n\n\n");
        System.out.println(auditCreateDTO.getText());
        latch.countDown();

    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String getPayload() {
        return payload;
    }
}
