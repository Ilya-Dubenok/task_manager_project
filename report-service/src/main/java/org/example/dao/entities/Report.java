package org.example.dao.entities;


import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "report")
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    private UUID uuid;

    @CreatedDate
    @Column(name = "dt_create")
    private LocalDateTime dtCreate;

    @LastModifiedDate
    @Version
    @Column(name = "dt_update")
    private LocalDateTime dtUpdate;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    private String description;

    @Type(JsonType.class)
    private Map<String, Object> params;


    public Report() {
    }

    public Report(UUID uuid) {
        this.uuid = uuid;
    }

    public Report(UUID uuid, LocalDateTime dtCreate, LocalDateTime dtUpdate, ReportStatus status, ReportType type, String description, Map<String, Object> params) {
        this.uuid = uuid;
        this.dtCreate = dtCreate;
        this.dtUpdate = dtUpdate;
        this.status = status;
        this.type = type;
        this.description = description;
        this.params = params;
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

    public LocalDateTime getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(LocalDateTime dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
