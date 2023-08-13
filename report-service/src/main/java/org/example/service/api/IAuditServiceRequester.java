package org.example.service.api;

import org.example.core.dto.audit.AuditDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IAuditServiceRequester {

    List<AuditDTO> getAuditDTOList(UUID uuid, LocalDate from, LocalDate to);


}
