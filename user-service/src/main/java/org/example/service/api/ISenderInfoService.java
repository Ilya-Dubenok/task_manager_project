package org.example.service.api;

import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;
import org.springframework.scheduling.annotation.Async;

public interface ISenderInfoService {

    @Async
    void sendAudit(User author, String text, Type type);

    class AuditMessages {

        public static String USER_CREATED_MESSAGE = "New user was created";

        public static String USER_UPDATED_MESSAGE = "User was updated";

        public static String USER_REGISTERED_MESSAGE = "User was registered";


    }

}
