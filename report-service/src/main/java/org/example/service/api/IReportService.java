package org.example.service.api;

import org.example.dao.entities.ReportType;

import java.util.Map;

public interface IReportService {

    void putReportRequest(Map<String, String> stringParams, ReportType type);

}
