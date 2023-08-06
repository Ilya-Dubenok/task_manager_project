package org.example.service.api;

import org.apache.commons.lang3.tuple.Pair;
import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;

import java.net.URI;
import java.util.Map;

public interface ISenderInfoService {



    void sendAudit(User author, String text, Type type, String id);

    void sendAudit(User author, Map<String, Pair<String, String>> updates, Type type, String id);

    void sendEmailAssignment(String to, String subject, String message);

    void sendEmailAssignmentWithReply(String to, String subject, String message, URI replyTo);


    class AuditMessages {

        public static String USER_CREATED_MESSAGE = "New user was created";

        public static String USER_UPDATED_MESSAGE = "User was updated";

        public static String USER_REGISTERED_MESSAGE = "User was registered";


    }

}
