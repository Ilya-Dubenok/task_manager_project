package org.example.service.api;


import org.example.core.dto.audit.AuditCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

@FeignClient(value = "auditservice", url = "${app.network.audit_service.host}${app.network.audit_service.appendix}")
public interface IAuditSenderFeignClient {



    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<?> createAudit(URI baseUrl, @RequestBody AuditCreateDTO auditCreateDTO);

}
