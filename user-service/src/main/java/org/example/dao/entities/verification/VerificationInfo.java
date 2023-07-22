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

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "email_status")
    private EmailStatus emailStatus;

    @Column(name = "count_of_attempts")
    private Integer countOfAttempts;


    public VerificationInfo() {
    }

    public VerificationInfo(UUID uuid, String mail, Integer code, LocalDateTime expirationTime, EmailStatus emailStatus) {
        this.uuid = uuid;
        this.mail = mail;
        this.code = code;
        this.expirationTime = expirationTime;
        this.emailStatus = emailStatus;
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

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
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
