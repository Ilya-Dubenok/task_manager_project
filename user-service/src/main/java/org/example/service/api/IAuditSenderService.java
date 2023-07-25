package org.example.service.api;


import org.example.config.property.ApplicationProperties;
import org.example.core.dto.audit.AuditCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "userservice", url = "${app.network.audit_service.host}${app.network.audit_service.appendix}")
public interface IAuditSenderService {


    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<?> createAudit(@RequestBody AuditCreateDTO auditCreateDTO);

}
