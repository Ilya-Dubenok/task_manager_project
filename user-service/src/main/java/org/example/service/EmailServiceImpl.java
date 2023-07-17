package org.example.service;

import org.example.service.api.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private JavaMailSender javaMailSender;

    private String appMail;

    //TODO REFACTOR THIS
    private static final String DEFAULT_VERIFICATION_CODE_TEXT_FORMAT=
            "Добрый день! Для завершения регистрации перейдите по ссылке ниже\n" +
                    "http://localhost:8081/task_manager/api/v1/users/verification/?code=%s&mail=%s";

    private static final String DEFAULT_VERIFICATION_SUBJECT = "Подтверждение регистрации в приложении TaskManager";

    public EmailServiceImpl(JavaMailSender javaMailSender, @Value("${mail.user}") String appMail) {
        this.javaMailSender = javaMailSender;
        this.appMail = appMail;

    }

    @Override
    public void sendMessage(String to, String text, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appMail);
        message.setTo(to);
        message.setText(text);
        message.setSubject(subject);
        javaMailSender.send(message);

    }

    @Override
    public void sendVerificationCodeMessage(String mail, Integer verificationCode) {

        String text = String.format(
                DEFAULT_VERIFICATION_CODE_TEXT_FORMAT, verificationCode, mail
        );

        sendMessage(mail,text, DEFAULT_VERIFICATION_SUBJECT);
    }
}
