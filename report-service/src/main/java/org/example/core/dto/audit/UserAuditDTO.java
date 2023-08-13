package org.example.core.dto.audit;

import org.example.core.dto.user.UserRole;

import java.util.UUID;

public class UserAuditDTO {

    private UUID uuid;


    private String mail;

    private String fio;

    private UserRole role;


    public UserAuditDTO() {
    }

    public UserAuditDTO(UUID uuid) {
        this.uuid = uuid;
    }

    public UserAuditDTO(UUID uuid, String mail, String fio, UserRole role) {
        this.uuid = uuid;
        this.mail = mail;
        this.fio = fio;
        this.role = role;

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

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }


}
