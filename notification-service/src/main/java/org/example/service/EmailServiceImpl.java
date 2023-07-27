package org.example.service;

import org.example.config.property.ApplicationProperties;
import org.example.core.dto.SimpleEmailTemplateDTO;
import org.example.service.api.IEmailService;
import org.example.service.api.IReplyEmailDeliveryStatusFeignClient;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


@Service
public class EmailServiceImpl implements IEmailService {

    private JavaMailSender javaMailSender;

    private String emailFrom;

    private IReplyEmailDeliveryStatusFeignClient replyEmailDeliveryStatusFeignClient;


    public EmailServiceImpl(JavaMailSender javaMailSender, ApplicationProperties property, IReplyEmailDeliveryStatusFeignClient replyEmailDeliveryStatusFeignClient) {
        this.javaMailSender = javaMailSender;
        this.emailFrom = property.getMail().getEmail();
        this.replyEmailDeliveryStatusFeignClient = replyEmailDeliveryStatusFeignClient;
    }

    @Override
    @Async
    public void sendMessage(SimpleEmailTemplateDTO simpleEmailTemplateDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(simpleEmailTemplateDTO.getTo());
        message.setText(simpleEmailTemplateDTO.getText());
        message.setSubject(simpleEmailTemplateDTO.getSubject());
        javaMailSender.send(message);

    }

    @Override
    @Async
    public void sendEmailWithFeedbackMessage(SimpleEmailTemplateDTO simpleEmailTemplateDTO) {

        Boolean success = true;

        try {
            sendMessage(simpleEmailTemplateDTO);
        } catch (MailException e) {

            success = false;

        }

        URI replyTo = simpleEmailTemplateDTO.getReplyTo();

        Map<String, Object> reply = new HashMap<>();

        reply.put("mail", simpleEmailTemplateDTO.getTo());
        reply.put("status", success);

        replyEmailDeliveryStatusFeignClient.sendStatusOfDelivery(
                replyTo, reply
        );


    }




}
