package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.dao.entities.audit.Audit;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IAuditService {


    void save(@Valid AuditCreateDTO auditCreateDTO);

    Audit getAuditById(UUID uuid);


    Page<Audit> getPageOfAudit(Integer currentRequestedPage, Integer rowsPerPage);

    List<Audit> getListOfAuditsForUserUuidAndTimeRange(UUID userUuid, LocalDate from, LocalDate to);


}
