package org.example.core.dto.report;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;

import java.util.UUID;

public class ReportDTO {

    private UUID uuid;

    private Long dtCreate;

    private Long dtUpdate;

    private ReportStatus status;

    private ReportType type;

    private String description;

    @JsonSerialize(using = ParamsSerializer.class)
    private Params params;

    public ReportDTO() {
    }

    public ReportDTO(UUID uuid, Long dtCreate, Long dtUpdate, ReportStatus status, ReportType type, String description, Params params) {
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

    public Long getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Long dtCreate) {
        this.dtCreate = dtCreate;
    }

    public Long getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Long dtUpdate) {
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

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }
}
