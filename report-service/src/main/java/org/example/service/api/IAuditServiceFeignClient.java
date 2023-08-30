package org.example.service.api;


import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.audit.Type;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(value = "audit-service", url = "http://url.placeholder")
public interface IAuditServiceFeignClient {


    @RequestMapping(method = RequestMethod.GET, consumes = "application/json")
    ResponseEntity<List<AuditDTO>> getAuditDTOList(URI baseUrl,
                                                   @RequestParam(name = "from") LocalDate from,
                                                   @RequestParam(name = "to") LocalDate to);

    @RequestMapping(method = RequestMethod.GET, value = "/{type}", consumes = "application/json")
    ResponseEntity<List<AuditDTO>> getAuditDTOList(URI baseUrl,
                                                   @PathVariable(name = "type") Type type,
                                                   @RequestParam(name = "id") UUID uuid,
                                                   @RequestParam(name = "from") LocalDate from,
                                                   @RequestParam(name = "to") LocalDate to);


}
