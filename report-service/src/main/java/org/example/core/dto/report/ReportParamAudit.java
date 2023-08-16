package org.example.core.dto.report;

import java.time.LocalDate;
import java.util.UUID;

public class ReportParamAudit implements Params {

    private UUID user;

    private LocalDate from;

    private LocalDate to;

    public ReportParamAudit() {
    }

    public ReportParamAudit(UUID user, LocalDate from, LocalDate to) {
        this.user = user;
        this.from = from;
        this.to = to;
    }

    public UUID getUser() {
        return user;
    }

    public void setUser(UUID user) {
        this.user = user;
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
