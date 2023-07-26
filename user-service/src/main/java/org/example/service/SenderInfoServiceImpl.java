package org.example.service;


import org.example.config.property.ApplicationProperties;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;
import org.example.service.api.IAuditServiceFeignClient;
import org.example.service.api.ISenderInfoService;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class SenderInfoServiceImpl implements ISenderInfoService {

    private final URI AUDIT_URL;

    private IAuditServiceFeignClient auditServiceFeignClient;

    private ApplicationProperties properties;

    private ConversionService conversionService;

    public static final String USER_CREATED_MESSAGE = "New user was created";


    public SenderInfoServiceImpl(IAuditServiceFeignClient auditServiceFeignClient,
                                 ApplicationProperties properties,
                                 ConversionService conversionService) {
        this.auditServiceFeignClient = auditServiceFeignClient;
        this.properties = properties;
        this.conversionService = conversionService;
        AUDIT_URL = getAuditUrl(properties);


    }


    @Async
    @Override
    public void sendAudit(User author, String text, Type type) {
        AuditUserDTO auditUserDTO = conversionService.convert(author, AuditUserDTO.class);


        AuditCreateDTO auditCreateDTO = new AuditCreateDTO(auditUserDTO, text, type);

        try {
            auditServiceFeignClient.createAudit(AUDIT_URL, auditCreateDTO);
        } catch (Exception e) {
            //TODO TRY LOGGING?
            e.printStackTrace();

        }

    }

    private URI getAuditUrl(ApplicationProperties properties) {
        final URI AUDIT_URL;
        ApplicationProperties.NetworkProp.AuditService auditService = properties.getNetwork().getAuditService();

        String prefix = "http://";

        if (auditService.getAddress() == null || auditService.getAddress().isBlank()) {

            AUDIT_URL = URI.create(

                    prefix + auditService.getHost() + auditService.getAppendix());
        } else {

            AUDIT_URL = URI.create(
                    prefix + auditService.getAddress() + auditService.getAppendix()
            );
        }
        return AUDIT_URL;
    }

}
