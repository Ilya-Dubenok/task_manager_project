package org.example.service;

import org.example.config.properties.ApplicationProperties;
import org.example.core.dto.audit.AuditDTO;
import org.example.core.exception.GeneralException;
import org.example.service.api.IAuditServiceFeignClient;
import org.example.service.api.IAuditServiceRequester;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AuditServiceRequesterImpl implements IAuditServiceRequester {


    private final String URL_PREFIX = "http://";

    private final URI AUDIT_SERVICE_INTERNAL_URL;

    private final IAuditServiceFeignClient auditServiceFeignClient;


    public AuditServiceRequesterImpl(IAuditServiceFeignClient auditServiceFeignClient, ApplicationProperties applicationProperties) {
        this.auditServiceFeignClient = auditServiceFeignClient;
        AUDIT_SERVICE_INTERNAL_URL = formAuditServiceUrl(applicationProperties);
    }

    @Override
    public List<AuditDTO> getAuditDTOList(UUID uuid, LocalDate from, LocalDate to) {
        try {

            URI uri = URI.create(AUDIT_SERVICE_INTERNAL_URL.toString().concat("/list"));
            ResponseEntity<List<AuditDTO>> response = auditServiceFeignClient.getAuditDTOList( uri,uuid,
                    from, to);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            return null;

        } catch (Exception e) {

            throw new GeneralException("При обработке запроса произошла ошибка", e);

        }
    }

    private URI formAuditServiceUrl(ApplicationProperties applicationProperties) {

        final URI AUDIT_SERVICE_URL;

        ApplicationProperties.NetworkProp.AuditService auditService = applicationProperties.getNetwork().getAuditService();

        if (auditService.getAddress() == null || auditService.getAddress().isBlank()) {

            AUDIT_SERVICE_URL = URI.create(

                    URL_PREFIX + auditService.getHost() + auditService.getInternalAppendix());
        } else {

            AUDIT_SERVICE_URL = URI.create(
                    URL_PREFIX + auditService.getAddress() + auditService.getInternalAppendix()
            );
        }
        return AUDIT_SERVICE_URL;

    }
}
