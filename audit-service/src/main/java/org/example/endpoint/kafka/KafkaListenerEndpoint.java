package org.example.endpoint.kafka;

import org.example.core.dto.audit.AuditCreateDTO;
import org.example.service.api.IAuditService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class KafkaListenerEndpoint {

    private IAuditService auditService;

    public KafkaListenerEndpoint(IAuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(
            topics = "AuditInfo"
    )
    public void listener(AuditCreateDTO auditCreateDTO) {
        auditService.save(auditCreateDTO);

    }

}
