package org.example.dao.entities;

import org.example.core.dto.report.Params;
import org.example.core.dto.report.ReportParamAudit;

public enum ReportType {

    JOURNAL_AUDIT(ReportParamAudit.class);

    private Class<? extends Params> targetClass;

    ReportType(Class<? extends Params> targetClass) {
        this.targetClass = targetClass;
    }

    public Class<? extends Params> getTargetClass() {
        return targetClass;
    }
}
