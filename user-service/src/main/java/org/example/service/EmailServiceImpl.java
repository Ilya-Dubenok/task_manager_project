package org.example.service;

import org.example.config.property.ApplicationProperties;
import org.example.dao.api.IVerificationInfoRepository;
import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.example.service.api.IEmailService;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class EmailServiceImpl implements IEmailService {

    private JavaMailSender javaMailSender;

    private String emailFrom;

    private String host;

    private TaskScheduler taskScheduler;

    private volatile ScheduledFuture<?> scheduledFuture;

    private ReentrantLock lock = new ReentrantLock();


    private IVerificationInfoRepository repository;


    private final String DEFAULT_VERIFICATION_CODE_TEXT_FORMAT =
            "Добрый день! Для завершения регистрации перейдите по ссылке ниже\n" +
                    "http://%s/user_service/api/v1/users/verification?code=%s&mail=%s";

    private static final String DEFAULT_VERIFICATION_SUBJECT = "Подтверждение регистрации в приложении TaskManager";

    public EmailServiceImpl(JavaMailSender javaMailSender, ApplicationProperties property, TaskScheduler taskScheduler, IVerificationInfoRepository repository) {
        this.javaMailSender = javaMailSender;
        this.emailFrom = property.getMail().getEmail();
        this.host = property.getNetwork().getHost();
        this.taskScheduler = taskScheduler;
        this.repository = repository;
    }

    @Override
    public void sendMessage(String to, String text, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(to);
        message.setText(text);
        message.setSubject(subject);
        javaMailSender.send(message);

    }

    @Override
    @Async
    public void sendVerificationCodeMessage(String mail, Integer verificationCode) {

        String text = String.format(
                DEFAULT_VERIFICATION_CODE_TEXT_FORMAT, host, verificationCode, mail
        );

        try {

            sendMessage(mail, text, DEFAULT_VERIFICATION_SUBJECT);
            repository.setEmailStatus(EmailStatus.SUCCESSFULLY_SENT.toString(), mail);


        } catch (MailException e) {
            repository.setEmailStatus(EmailStatus.FAILURE_ON_SENT.toString(), mail);
            startScheduledRepeatableSendingIfNeeded();

        }

    }



    private void startScheduledRepeatableSendingIfNeeded() {

        lock.lock();
        if (scheduledFuture == null) {
            scheduledFuture = taskScheduler.scheduleAtFixedRate(
                    this::scheduledRepeatableSending,
                    Instant.now().plusSeconds(20),
                    Duration.ofSeconds(60)
            );

        }
        lock.unlock();

    }

    private void scheduledRepeatableSending() {
        List<VerificationInfo> failedEmails = repository.findByEmailStatusIsAndCountOfAttemptsIsLessThan(
                EmailStatus.FAILURE_ON_SENT, 5
        );

        if (failedEmails.size() == 0) {
            lock.lock();
            scheduledFuture.cancel(true);
            scheduledFuture = null;
            lock.unlock();
            return;

        }

        failedEmails.forEach(x -> sendVerificationCodeMessageWithoutFurtherScheduling(x.getMail(), x.getCode()));


    }


    private void sendVerificationCodeMessageWithoutFurtherScheduling(String mail, Integer verificationCode) {

        String text = String.format(
                DEFAULT_VERIFICATION_CODE_TEXT_FORMAT, verificationCode, mail
        );

        try {

            sendMessage(mail, text, DEFAULT_VERIFICATION_SUBJECT);
            repository.setEmailStatus(EmailStatus.SUCCESSFULLY_SENT.toString(), mail);


        } catch (MailException ignored) {

            repository.increaseCountOfFailedAttempts(mail);

        }

    }

}
