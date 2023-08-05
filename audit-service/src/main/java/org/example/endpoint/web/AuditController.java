package org.example.endpoint.web;


import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.audit.PageOfTypeDTO;
import org.example.dao.entities.audit.Audit;
import org.example.service.api.IAuditService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private IAuditService auditService;
    private ConversionService conversionService;



    public AuditController(IAuditService auditService, ConversionService conversionService) {
        this.auditService = auditService;
        this.conversionService = conversionService;
    }


    @GetMapping(value = "/{uuid}")
    public ResponseEntity<AuditDTO> getAuditByUuid(@PathVariable UUID uuid) {

        Audit audit = auditService.getAuditById(uuid);
        AuditDTO dto = conversionService.convert(audit, AuditDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageOfTypeDTO<AuditDTO>> getPAgeOfAudit(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size){

        Page<Audit> pageOfAudit = auditService.getPageOfAudit(page, size);

        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                PageOfTypeDTO.class, AuditDTO.class
        );

        PageOfTypeDTO<AuditDTO> converted = (PageOfTypeDTO<AuditDTO>) conversionService.convert(
                pageOfAudit, TypeDescriptor.valueOf(PageImpl.class),
                new TypeDescriptor(resolvableType, null, null)
        );

        return new ResponseEntity<>(
                converted, HttpStatus.OK
        );


    }


}
