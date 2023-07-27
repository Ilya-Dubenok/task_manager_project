package org.example.service.api;

import org.example.core.dto.SimpleEmailTemplateDTO;
import org.springframework.scheduling.annotation.Async;

public interface IEmailService {


    @Async
    void sendMessage(SimpleEmailTemplateDTO simpleEmailTemplateDTO);

    @Async
    void sendEmailWithFeedbackMessage(SimpleEmailTemplateDTO simpleEmailTemplateDTO);
}
