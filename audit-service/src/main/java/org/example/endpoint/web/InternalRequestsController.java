package org.example.endpoint.web;


import org.example.core.dto.audit.AuditDTO;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
import org.example.service.api.IAuditService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class InternalRequestsController {

    private IAuditService auditService;

    private ConversionService conversionService;

    public InternalRequestsController(IAuditService auditService, ConversionService conversionService) {
        this.auditService = auditService;
        this.conversionService = conversionService;
    }

    @GetMapping(value = "/list")
    public ResponseEntity<List<AuditDTO>> getForTimeRange(
            @RequestParam(value = "from") LocalDate from,
            @RequestParam(value = "to") LocalDate to) {

        List<Audit> audits = auditService.getListOfAuditsForTimeRange(from, to);

        List<AuditDTO> res = new ArrayList<>();

        for (Audit audit : audits) {

            res.add(conversionService.convert(audit, AuditDTO.class));
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping(value = "/list/{type}")
    public ResponseEntity<List<AuditDTO>> getByUuid(
            @PathVariable Type type,
            @RequestParam(value = "id") String id,
            @RequestParam(value = "from") LocalDate from,
            @RequestParam(value = "to") LocalDate to) {

        List<Audit> audits = auditService.getListOfAuditsForTypeAndIdAndTimeRange(type, id, from, to);

        List<AuditDTO> res = new ArrayList<>();

        for (Audit audit : audits) {

            res.add(conversionService.convert(audit, AuditDTO.class));
        }

        return new ResponseEntity<>(res, HttpStatus.OK);

    }

}
