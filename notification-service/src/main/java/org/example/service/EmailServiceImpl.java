package org.example.service;

import org.example.config.property.ApplicationProperties;
import org.example.core.dto.SimpleEmailTemplateDTO;
import org.example.service.api.IEmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements IEmailService {

    private JavaMailSender javaMailSender;

    private String emailFrom;


    public EmailServiceImpl(JavaMailSender javaMailSender, ApplicationProperties property) {
        this.javaMailSender = javaMailSender;
        this.emailFrom = property.getMail().getEmail();
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
//        catch (MailException e)

    }

    @Override
    @Async
    public void sendEmailWithFeedbackMessage(SimpleEmailTemplateDTO simpleEmailTemplateDTO) {



    }




}
