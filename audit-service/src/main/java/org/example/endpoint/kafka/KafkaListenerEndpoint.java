package org.example.endpoint.kafka;

import org.example.core.dto.AuditCreateDTO;
import org.example.service.api.IAuditService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerEndpoint {

    private IAuditService auditService;

    public KafkaListenerEndpoint(IAuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(
            topics = "audit_info"
    )
    public void listener(AuditCreateDTO auditCreateDTO) {
        auditService.save(auditCreateDTO);

    }

}
