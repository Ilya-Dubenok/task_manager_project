package org.example.service;

import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.audit.Type;
import org.example.service.api.IAuditSenderKafkaClient;
import org.example.service.api.IAuditService;
import org.example.service.api.IUserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditServiceImpl implements IAuditService {


    private IUserService userService;

    private IAuditSenderKafkaClient<String, Object> auditSenderKafkaClient;

    public AuditServiceImpl(IUserService userService, IAuditSenderKafkaClient<String, Object> auditSenderKafkaClient) {
        this.userService = userService;
        this.auditSenderKafkaClient = auditSenderKafkaClient;
    }

    @Override
    @Async
    public void sendAudit(UUID authorUuid, String text, Type type, String id) {

        AuditUserDTO author = userService.findAuditUserDTOInfoInCurrentContext(authorUuid);
        AuditCreateDTO auditCreateDTO = new AuditCreateDTO(author, text, type, id);

        try {
            auditSenderKafkaClient.send("AuditInfo", auditCreateDTO);

        } catch (Exception e) {
            //TODO TRY LOGGING?
            e.printStackTrace();

        }

    }
}
