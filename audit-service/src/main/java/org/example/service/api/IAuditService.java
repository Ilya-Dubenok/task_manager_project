package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.AuditCreateDTO;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.user.UserRole;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IAuditService {


    void save(@Valid AuditCreateDTO auditCreateDTO);

    Audit getAuditById(UUID uuid);


    Page<Audit> getPageOfAudit(Integer currentRequestedPage, Integer rowsPerPage);



}