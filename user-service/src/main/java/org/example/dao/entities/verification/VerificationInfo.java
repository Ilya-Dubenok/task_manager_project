package org.example.dao.entities.verification;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_info",
    uniqueConstraints = @UniqueConstraint(name = "verification_mail_unique_constraint", columnNames = "mail")
)
public class VerificationInfo {


    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String mail;

    @Column(nullable = false)
    private Integer code;

    @Column(name = "registered_time")
    private LocalDateTime registeredTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "email_status")
    private EmailStatus emailStatus;

    @Column(name = "count_of_attempts")
    private Integer countOfAttempts;


    public VerificationInfo() {
    }

    public VerificationInfo(UUID uuid, String mail, Integer code, LocalDateTime registeredTime, EmailStatus emailStatus,
                            Integer countOfAttempts) {
        this.uuid = uuid;
        this.mail = mail;
        this.code = code;
        this.registeredTime = registeredTime;
        this.emailStatus = emailStatus;
        this.countOfAttempts = countOfAttempts;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public LocalDateTime getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(LocalDateTime registeredTime) {
        this.registeredTime = registeredTime;
    }

    public EmailStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(EmailStatus emailStatus) {
        this.emailStatus = emailStatus;
    }

    public Integer getCountOfAttempts() {
        return countOfAttempts;
    }

    public void setCountOfAttempts(Integer countOfAttempts) {
        this.countOfAttempts = countOfAttempts;
    }
}
