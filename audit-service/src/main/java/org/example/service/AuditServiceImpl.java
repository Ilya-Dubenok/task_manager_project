package org.example.service;


import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IAuditRepository;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
import org.example.service.api.IAuditService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Validated
public class AuditServiceImpl implements IAuditService {


    private IAuditRepository auditRepository;

    private ConversionService conversionService;


    public AuditServiceImpl(IAuditRepository auditRepository, ConversionService conversionService) {
        this.auditRepository = auditRepository;
        this.conversionService = conversionService;
    }


    @Override
    @Transactional
    public void save(@Valid AuditCreateDTO auditCreateDTO) {
        Audit toRegister = conversionService.convert(auditCreateDTO, Audit.class);

        auditRepository.save(toRegister);

    }

    @Override
    @Transactional(readOnly = true)
    public Audit getAuditById(UUID uuid) {
        return auditRepository.findByUuid(uuid)
                .orElseThrow(
                        () -> new GeneralException("Не найдена запись по такому uuid")
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Audit> getPageOfAudit(Integer currentRequestedPage, Integer rowsPerPage) {
        StructuredException exception = new StructuredException();

        if (currentRequestedPage < 0) {

            exception.put("page", "Номер страницы не может быть меньше 0");

        }
        if (rowsPerPage < 1) {
            exception.put("size", "Размер страницы не может быть меньше 0");

        }

        if (exception.hasExceptions()) {
            throw exception;
        }

        Page<Audit> page = auditRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

        return page;

    }

    @Override
    @Transactional(readOnly = true)
    public List<Audit> getListOfAuditsForTimeRange(LocalDate from, LocalDate to) {
        return auditRepository.findAll
                ((root, query, builder) -> {

                    Path<LocalDate> dtCreate = root.get("dtCreate");

                    Predicate before = builder.lessThanOrEqualTo(dtCreate, to);

                    Predicate after = builder.greaterThanOrEqualTo(dtCreate, from);

                    return builder.and(before, after);

                    }
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Audit> getListOfAuditsForTypeAndIdAndTimeRange(Type type, String id, LocalDate from, LocalDate to) {
        return auditRepository.findAll(
                ((root, query, builder) -> {
                    

                    Predicate target;

                    if (type.equals(Type.USER)) {
                        Path<UUID> typePersistedId = root.get("user").get("uuid");
                        target = builder.equal(typePersistedId, UUID.fromString(id));

                    } else {

                        Predicate typeEqual = builder.equal(root.get("type"), type);
                        Predicate idEqual = builder.equal(root.get("id"), id);
                        target = builder.and(typeEqual, idEqual);

                    }

                    Path<LocalDate> dtCreate = root.get("dtCreate");

                    Predicate before = builder.lessThanOrEqualTo(dtCreate, to);

                    Predicate after = builder.greaterThanOrEqualTo(dtCreate, from);

                    return builder.and(target, before, after);

                })
        );
    }
}
