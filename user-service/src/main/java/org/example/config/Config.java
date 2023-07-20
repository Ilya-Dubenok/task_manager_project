package org.example.config;


import org.example.config.property.ConfidentialProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Properties;

@Configuration
public class Config implements WebMvcConfigurer {

    @Bean
    @ConfigurationProperties("app")
    public ConfidentialProperties getProperty() {
        return new ConfidentialProperties();
    }

    @Bean
    public JavaMailSender getJavaMailSender(ConfidentialProperties property) {
        ConfidentialProperties.MailProp mailProp = property.getMail();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.mail.ru");
        mailSender.setPort(587);
        mailSender.setUsername(mailProp.getEmail());
        mailSender.setPassword(mailProp.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.sender", mailProp.getEmail());
        return mailSender;
    }





}
