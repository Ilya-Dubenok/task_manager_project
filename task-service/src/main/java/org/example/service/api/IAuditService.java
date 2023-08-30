package org.example.service.api;

import org.example.core.dto.audit.Type;

import java.util.UUID;

public interface IAuditService {

    void sendAudit(UUID authorUuid, String text, Type type, String id);

}
