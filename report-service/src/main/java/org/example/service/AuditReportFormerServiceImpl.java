package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.core.exception.ReportFormingFailedException;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.example.service.api.IAuditReportFormerService;
import org.example.service.api.IAuditServiceRequester;
import org.example.dao.api.IFileRepository;
import org.example.service.api.IReportService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
public class AuditReportFormerServiceImpl implements IAuditReportFormerService {


    private IReportService reportService;

    private ConversionService conversionService;

    private IAuditServiceRequester auditServiceRequester;

    private IFileRepository fileRepositoryService;

    private Function<UUID, String> fileNameFromUuidFunction = (x)-> x.toString().concat(".xlsx");

    private Function<ReportType, String> bucketNameFromReportTypeFunction = (x)-> "journal_audit";

    public AuditReportFormerServiceImpl(IReportService reportService, ConversionService conversionService, IAuditServiceRequester auditServiceRequester, IFileRepository fileRepositoryService) {
        this.reportService = reportService;
        this.conversionService = conversionService;
        this.auditServiceRequester = auditServiceRequester;
        this.fileRepositoryService = fileRepositoryService;
    }

    @Scheduled(fixedDelay = 30000)
    public void performScheduledReportFormingAndSending() {

        List<Report> reports = reportService.getReportsWithTypeAndStatus(ReportType.JOURNAL_AUDIT, ReportStatus.LOADED);

        for (Report report : reports) {

            UUID uuid = report.getUuid();

            try {

                reportService.setStatus(uuid, ReportStatus.PROGRESS);

                formAndSendReport(report);

                reportService.setStatus(uuid, ReportStatus.DONE);

            } catch (ReportFormingFailedException e) {

                reportService.setStatusFailed(uuid);
            }


        }


    }

    private void formAndSendReport(Report report) {

        Map<String, Object> params = report.getParams();

        String fileName = null;

        try {

            ReportParamAudit reportParamAudit = (ReportParamAudit) conversionService.convert(
                    params,
                    TypeDescriptor.valueOf(Map.class),
                    TypeDescriptor.valueOf(ReportParamAudit.class)
            );

            List<AuditDTO> auditDTOList = auditServiceRequester.getAuditDTOList(
                    reportParamAudit.getType(),
                    reportParamAudit.getId(),
                    reportParamAudit.getFrom(),
                    reportParamAudit.getTo().plusDays(1)
            );

            UUID reportUuid = report.getUuid();

            fileName = createFileWithReports(auditDTOList, reportUuid, this.fileNameFromUuidFunction);

            String bucketName = formBucketName(report.getType(), this.bucketNameFromReportTypeFunction);

            fileRepositoryService.saveFile(fileName, bucketName);

            reportService.saveReportInfo(reportUuid, fileName, bucketName);



        } catch (Exception e) {

            //TODO place for logging

            throw new ReportFormingFailedException(e.getMessage());

        } finally {

            if (fileName != null) {

                File file = new File(fileName);

                file.delete();
            }


        }


    }


    private <T> String createFileWithReports(List<AuditDTO> auditDTOList, T source, Function<T, String> formReportFileName) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()){

            Sheet sheet = workbook.createSheet("report");
            int rowNum = 0;

            createTopRow(workbook, sheet, rowNum++);

            createHeaderRow(workbook, sheet, rowNum++);

            for (AuditDTO auditDTO : auditDTOList) {

                fillRowWithData(auditDTO, workbook, sheet, rowNum++);

            }

            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            String fileName = fileNameFormer(source, formReportFileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

                workbook.write(fileOutputStream);

                workbook.close();

            }

            return fileName;

        }

    }

    private <T> String fileNameFormer(T source,Function<T, String> formReportFileNameFunction) {

        return formReportFileNameFunction.apply(source);

    }


    private <T> String formBucketName(T source, Function<T, String> formBucketFileNameFunction) {

        return formBucketFileNameFunction.apply(source);

    }

    private void fillRowWithData(AuditDTO auditDTO, Workbook workbook, Sheet sheet, int rowNum) {

        Row contextRow = sheet.createRow(rowNum);

        contextRow.createCell(0).setCellValue(auditDTO.getUuid().toString());

        CellStyle dtUpdateCellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        dtUpdateCellStyle.setDataFormat(
                creationHelper.createDataFormat().getFormat("dd-mm-yyyy h:mm:ss"));

        Cell dtUpdateCell = contextRow.createCell(1);
        dtUpdateCell.setCellStyle(dtUpdateCellStyle);
        Long dtCreate = auditDTO.getDtCreate();
        LocalDateTime converterLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dtCreate),
                TimeZone.getDefault().toZoneId());
        dtUpdateCell.setCellValue(converterLocalDateTime);


        contextRow.createCell(2).setCellValue(auditDTO.getUser().getUuid().toString());
        contextRow.createCell(3).setCellValue(auditDTO.getUser().getMail());
        contextRow.createCell(4).setCellValue(auditDTO.getUser().getFio());
        contextRow.createCell(5).setCellValue(auditDTO.getUser().getRole().toString());
        contextRow.createCell(6).setCellValue(auditDTO.getText());
        contextRow.createCell(7).setCellValue(auditDTO.getType().toString());
        contextRow.createCell(8).setCellValue(auditDTO.getId());

    }

    private static void createHeaderRow(Workbook workbook, Sheet sheet, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        headerRow.createCell(0).setCellValue("uuid");
        headerRow.createCell(1).setCellValue("dt_create");
        headerRow.createCell(2).setCellValue("uuid");
        headerRow.createCell(3).setCellValue("mail");
        headerRow.createCell(4).setCellValue("fio");
        headerRow.createCell(5).setCellValue("role");
        headerRow.createCell(6).setCellValue("text");
        headerRow.createCell(7).setCellValue("type");
        headerRow.createCell(8).setCellValue("id");
    }

    private static void createTopRow(Workbook workbook, Sheet sheet, int rowNum) {
        Row userHeaderRow = sheet.createRow(rowNum);
        Cell userCell = userHeaderRow.createCell(2);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        userCell.setCellStyle(cellStyle);
        userCell.setCellValue("user");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 5));

    }
}
