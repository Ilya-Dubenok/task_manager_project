package org.example.service.api;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;

import java.net.URI;

public interface ISenderInfoService {


//    @KafkaListener(topics = "transaction-1")
//    void listener(@Payload AuditCreateDTO auditCreateDTO,
//                  ConsumerRecord<String, AuditCreateDTO> cr);

    @Async
    void sendAudit(User author, String text, Type type, String id);

    @Async
    void sendEmailAssignment(String to, String subject, String message);

    @Async
    void sendEmailAssignmentWithReply(String to, String subject, String message, URI replyTo);


    class AuditMessages {

        public static String USER_CREATED_MESSAGE = "New user was created";

        public static String USER_UPDATED_MESSAGE = "User was updated";

        public static String USER_REGISTERED_MESSAGE = "User was registered";


    }

}
