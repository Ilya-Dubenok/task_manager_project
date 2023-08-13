package org.example.service.api;

import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IReportService {

    void putReportRequest(Map<String, String> stringParams, ReportType type);

    List<Report> getReportsWithTypeAndStatus(ReportType type, ReportStatus status);

    boolean isReportAvailable(UUID reportUuid);

}
