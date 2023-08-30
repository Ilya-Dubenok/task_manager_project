package org.example.service.api;

import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.audit.Type;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IAuditServiceRequester {

    List<AuditDTO> getAuditDTOList(Type type, UUID id, LocalDate from, LocalDate to);


}
