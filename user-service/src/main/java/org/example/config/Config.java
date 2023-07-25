package org.example.config;


import feign.Feign;
import feign.codec.Decoder;
import org.example.config.property.ApplicationProperties;
import org.example.service.api.IAuditSenderService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Properties;

@Configuration
public class Config implements WebMvcConfigurer {

    @Bean
    @ConfigurationProperties("app")
    public ApplicationProperties getProperty() {
        return new ApplicationProperties();
    }

    @Bean
    public JavaMailSender getJavaMailSender(ApplicationProperties property) {
        ApplicationProperties.MailProp mailProp = property.getMail();
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

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setMaxPoolSize(6);
        threadPoolTaskExecutor.setQueueCapacity(15);
        return threadPoolTaskExecutor;
    }



}
