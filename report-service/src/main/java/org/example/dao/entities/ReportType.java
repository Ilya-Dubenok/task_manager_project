package org.example.dao.entities;

import org.example.core.dto.report.Params;
import org.example.core.dto.report.ReportParamAudit;

import java.util.Map;

public enum ReportType {

    JOURNAL_AUDIT(ReportParamAudit.class, "Журнал аудита за: %s - %s");

    private Class<? extends Params> targetClass;

    private final String reportTypeFormat;

    ReportType(Class<? extends Params> targetClass, String reportTypeFormat) {
        this.targetClass = targetClass;
        this.reportTypeFormat = reportTypeFormat;
    }

    public Class<? extends Params> getTargetClass() {
        return targetClass;
    }

    public String getReportDescription(Map<String, String> params) {

        String from = params.get("from");
        String to = params.get("to");

        return String.format(reportTypeFormat, from, to);
    }

    public String getReportFileName(Report report) {
        return report.getUuid().toString().concat(".xlsx");
    }


    public String getReportFileType() {
        return "journal_audit";
    }
}
