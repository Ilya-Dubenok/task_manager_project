package org.example.service;


import jakarta.validation.Valid;
import org.example.core.dto.AuditCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IAuditRepository;
import org.example.dao.entities.audit.Audit;
import org.example.service.api.IAuditService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
    public void save(@Valid AuditCreateDTO auditCreateDTO) {
        Audit toRegister = conversionService.convert(auditCreateDTO, Audit.class);

        try {
            auditRepository.save(toRegister);
        } catch (Exception e) {
            StructuredException structuredException = new StructuredException();

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


    }

    @Override
    public Audit getAuditById(UUID uuid) {
        return auditRepository.findByUuid(
                uuid
        ).orElseThrow(
                () -> new GeneralException("Не найдена запись по такому uuid")
        );
    }

    @Override
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

        try {

            Page<Audit> page = auditRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

            return page;

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

    }
}
