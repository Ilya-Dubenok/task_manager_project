package org.example.service.api;

import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IReportService {

    void putReportRequest(Map<String, String> stringParams, ReportType type);

    List<Report> getReportsWithTypeAndStatus(ReportType type, ReportStatus status);

    boolean isReportAvailable(UUID reportUuid);

    void setStatusFailed(UUID reportUuid);

    void setStatus(UUID uuid, ReportStatus reportStatus);

    String getReportFileUrl(UUID uuid);

    void saveReportInfo(UUID reportUuid, String fileName, String bucketName);

    @Transactional(readOnly = true)
    Page<Report> getPageOfReports(Integer page, Integer size);
}
