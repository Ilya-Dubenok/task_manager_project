package org.example.service;


import org.example.config.property.ApplicationProperties;
import org.example.core.dto.email.EmailDTO;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;
import org.example.service.api.IAuditSenderKafkaClient;
import org.example.service.api.INotificationServiceFeignClient;
import org.example.service.api.ISenderInfoService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class SenderInfoServiceImpl implements ISenderInfoService {


    private final String prefix = "http://";

    private final URI NOTIFICATION_SERVICE_URL;

    private INotificationServiceFeignClient notificationServiceFeignClient;

    private IAuditSenderKafkaClient<String, Object> auditSenderKafkaClient;

    private ConversionService conversionService;


    public SenderInfoServiceImpl(INotificationServiceFeignClient notificationServiceFeignClient,
                                 IAuditSenderKafkaClient<String, Object> auditSenderKafkaClient,
                                 ApplicationProperties properties,
                                 ConversionService conversionService) {
        this.notificationServiceFeignClient = notificationServiceFeignClient;
        this.auditSenderKafkaClient = auditSenderKafkaClient;
        this.conversionService = conversionService;
        NOTIFICATION_SERVICE_URL = getNotificationUrl(properties);

    }


    @Override
    public void sendAudit(User author, String text, Type type, String id) {
        AuditUserDTO auditUserDTO = conversionService.convert(author, AuditUserDTO.class);


        AuditCreateDTO auditCreateDTO = new AuditCreateDTO(auditUserDTO, text, type, id);

        try {
            auditSenderKafkaClient.send("AuditInfo", auditCreateDTO);

        } catch (Exception e) {
            //TODO TRY LOGGING?
            e.printStackTrace();

        }

    }


    @Override
    public void sendEmailAssignment(String to, String subject, String message) {
        try {
            notificationServiceFeignClient.sendEmail(NOTIFICATION_SERVICE_URL, new EmailDTO(
                    to, subject, message,
                    null));

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void sendEmailAssignmentWithReply(String to, String subject, String message, URI replyTo) {
        try {
            notificationServiceFeignClient.sendEmail(NOTIFICATION_SERVICE_URL, new EmailDTO(
                    to, subject, message,
                    replyTo));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private URI getAuditUrl(ApplicationProperties properties) {
        final URI AUDIT_URL;
        ApplicationProperties.NetworkProp.AuditService auditService = properties.getNetwork().getAuditService();

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


    private URI getNotificationUrl(ApplicationProperties properties) {
        final URI NOTIFICATION_URL;
        ApplicationProperties.NetworkProp.NotificationService notificationService = properties.getNetwork().getNotificationService();

        if (notificationService.getAddress() == null || notificationService.getAddress().isBlank()) {

            NOTIFICATION_URL = URI.create(

                    prefix + notificationService.getHost() + notificationService.getAppendix());
        } else {

            NOTIFICATION_URL = URI.create(
                    prefix + notificationService.getAddress() + notificationService.getAppendix()
            );
        }
        return NOTIFICATION_URL;

    }
}
