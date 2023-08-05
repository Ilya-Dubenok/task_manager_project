package org.example.dao.entities.audit;


import jakarta.persistence.*;
import org.example.core.dto.user.UserAuditDTO;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Audit {

    @Id
    private UUID uuid;

    @CreatedDate
    @Column(name = "dt_create")
    private LocalDateTime dtCreate;

    @Embedded
    private UserAuditDTO user;

    private String text;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String id;

    public Audit() {
    }

    public Audit(UUID uuid) {
        this.uuid = uuid;
    }

    public Audit(UUID uuid, UserAuditDTO user, String text, Type type, String id) {
        this.uuid = uuid;
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

    public LocalDateTime getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(LocalDateTime dtCreate) {
        this.dtCreate = dtCreate;
    }

    public UserAuditDTO getUser() {
        return user;
    }

    public void setUser(UserAuditDTO user) {
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
