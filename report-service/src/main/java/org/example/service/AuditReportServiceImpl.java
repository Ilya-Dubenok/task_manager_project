package org.example.service;

import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.core.exception.ReportFormingFailedException;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.example.service.api.IAuditReportService;
import org.example.service.api.IReportService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditReportServiceImpl implements IAuditReportService {

    private HashMap<String, LocalDateTime> storedReportsData = new HashMap<>();

    private IReportService reportService;

    private ConversionService conversionService;

    public AuditReportServiceImpl(IReportService reportService, ConversionService conversionService) {
        this.reportService = reportService;
        this.conversionService = conversionService;
    }

    public void performScheduledReportFormingAndSending() {

        List<Report> reports = reportService.getReportsWithTypeAndStatus(ReportType.JOURNAL_AUDIT, ReportStatus.LOADED);

        for (Report report : reports) {

            try {

                formAndSendReport(report);

            } catch (ReportFormingFailedException e) {


                //TODO ADD LOGIC IN THIS POINT
            }


        }


    }

    private void formAndSendReport(Report report) {

        Map<String, Object> params = report.getParams();

        try {

            ReportParamAudit reportParamAudit = (ReportParamAudit) conversionService.convert(
                    params,
                    TypeDescriptor.valueOf(Map.class),
                    TypeDescriptor.valueOf(ReportParamAudit.class)
            );

//            List<AuditDTO> auditDTOList = auditClient.getAuditDTOList(
//                    reportParamAudit.getUser(),
//                    reportParamAudit.getFrom(),
//                    reportParamAudit.getTo()
//            );


        } catch (Exception e) {

            throw new ReportFormingFailedException(e.getMessage());

        }


    }

}
