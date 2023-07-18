package org.example.config;


import org.example.config.property.ConfidentialProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class Config {

    @Bean
    @ConfigurationProperties("app")
    public ConfidentialProperties getProperty() {
        return new ConfidentialProperties();
    }

    @Bean
    public JavaMailSender getJavaMailSender(ConfidentialProperties property) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.mail.ru");
        mailSender.setPort(587);
        mailSender.setUsername(property.getMail().getEmail());
        mailSender.setPassword(property.getMail().getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.sender", property.getMail().getEmail());
        return mailSender;
    }


}
