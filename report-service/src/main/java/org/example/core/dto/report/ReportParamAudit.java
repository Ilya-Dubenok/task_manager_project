package org.example.core.dto.report;

import org.example.core.dto.audit.Type;

import java.time.LocalDate;
import java.util.UUID;

public class ReportParamAudit implements Params {

    private Type type;

    private UUID id;

    private LocalDate from;

    private LocalDate to;

    public ReportParamAudit() {
    }

    public ReportParamAudit(Type type, UUID id, LocalDate from, LocalDate to) {
        this.type = type;
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
