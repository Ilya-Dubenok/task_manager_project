package org.example.service.api;

import org.apache.commons.lang3.tuple.Pair;
import org.example.core.dto.audit.Type;
import org.example.dao.entities.user.User;

import java.net.URI;
import java.util.Map;

public interface ISenderInfoService {

    void sendAudit(User author, String text, Type type, String id);

    void sendEmailAssignment(String to, String subject, String message);

    void sendEmailAssignmentWithReply(String to, String subject, String message, URI replyTo);


 }
