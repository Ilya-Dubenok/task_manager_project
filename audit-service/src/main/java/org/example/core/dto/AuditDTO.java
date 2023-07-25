package org.example.core.dto;

import org.example.dao.entities.audit.Type;
import org.example.dao.entities.user.User;

import java.util.UUID;

public class AuditDTO {

    private UUID uuid;

    private Long dtCreate;

    private User user;

    private String text;

    private Type type;

    private String id;

    public AuditDTO() {
    }

    public AuditDTO(UUID uuid, Long dtCreate, User user, String text, Type type, String id) {
        this.uuid = uuid;
        this.dtCreate = dtCreate;
        this.user = user;
        this.text = text;
        this.type = type;
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Long getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Long dtCreate) {
        this.dtCreate = dtCreate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
