package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IAuditService {


    void save(@Valid AuditCreateDTO auditCreateDTO);

    Audit getAuditById(UUID uuid);


    Page<Audit> getPageOfAudit(Integer currentRequestedPage, Integer rowsPerPage);

    List<Audit> getListOfAuditsForTimeRange(LocalDate from, LocalDate to);

    List<Audit> getListOfAuditsForTypeAndIdAndTimeRange(Type type, String id, LocalDate from, LocalDate to);


}
