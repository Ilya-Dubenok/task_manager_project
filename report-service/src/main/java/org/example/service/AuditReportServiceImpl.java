package org.example.service;

import io.minio.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.core.exception.ReportFormingFailedException;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.example.service.api.IAuditReportService;
import org.example.service.api.IAuditServiceRequester;
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

@Service
public class AuditReportServiceImpl implements IAuditReportService {

    private HashMap<String, LocalDateTime> storedReportsData = new HashMap<>();

    private IReportService reportService;

    private ConversionService conversionService;

    private IAuditServiceRequester auditServiceRequester;

    public AuditReportServiceImpl(IReportService reportService, ConversionService conversionService, IAuditServiceRequester auditServiceRequester) {
        this.reportService = reportService;
        this.conversionService = conversionService;
        this.auditServiceRequester = auditServiceRequester;
    }

    @Scheduled(fixedDelay = 30000)
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

            List<AuditDTO> auditDTOList = auditServiceRequester.getAuditDTOList(
                    reportParamAudit.getUser(),
                    reportParamAudit.getFrom(),
                    reportParamAudit.getTo().plusDays(1)
            );

            String fileName = createFileWithReports(auditDTOList, report.getUuid());


            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("http://127.0.0.1:9000")
                            .credentials("minio_user", "minio_password")
                            .build();

            boolean b = minioClient.bucketExists(
                    BucketExistsArgs
                            .builder()
                            .bucket("user2")
                            .build()
            );

            if (!b){

                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket("user2")
                                .build());

            }


            minioClient.uploadObject(
                    UploadObjectArgs
                            .builder()
                            .bucket("user2")
                            .object(fileName)
                            .filename(fileName)
                            .build()
                    );


        } catch (Exception e) {

            throw new ReportFormingFailedException(e.getMessage());

        }


    }


    private String createFileWithReports(List<AuditDTO> auditDTOList, UUID reportUuid) throws IOException {

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

            String fileName = reportUuid.toString().concat(".xlsx");

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

                workbook.write(fileOutputStream);

                workbook.close();

            }

            return fileName;

        }

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
