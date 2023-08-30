package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.audit.Type;
import org.example.core.dto.audit.UserAuditDTO;
import org.example.core.dto.report.ReportDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IReportRepository;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.example.service.api.IReportService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class ReportServiceTest {

    private static final UUID SAMPLE_REPORT_UUID = UUID.randomUUID();


    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IReportRepository reportRepository;

    @Autowired
    private IReportService reportService;

    @Autowired
    private ConversionService conversionService;

    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IReportRepository reportRepository) {

        clearAndInitSchema(dataSource);
        fillProjectTableWithDefaultValues(reportRepository);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, reportRepository);
        }

    }


    @Test
    public void testConversionToDTOWorks() {

        Report report = reportRepository.findById(SAMPLE_REPORT_UUID).orElseThrow();

        Assertions.assertDoesNotThrow(() -> conversionService.convert(report, ReportDTO.class));

    }


    @Test
    public void testConversionToReportParamsAuditWorks() {

        Report report = reportRepository.findById(SAMPLE_REPORT_UUID).orElseThrow();

        Assertions.assertDoesNotThrow(() -> {
            ReportParamAudit res = (ReportParamAudit) conversionService.convert(report.getParams(), TypeDescriptor.valueOf(Map.class), TypeDescriptor.valueOf(ReportParamAudit.class));
            return res;
        });

    }


    @Test
    public void testConversionFromMapToMapWorks() {

        Map<String, String> params = new HashMap<>();

        UUID userUuid = UUID.randomUUID();

        params.put("user", userUuid.toString());
        params.put("from", LocalDate.now().toString());
        params.put("to", LocalDate.now().plusDays(2).toString());

        ResolvableType type = ResolvableType.forClassWithGenerics(Map.class, ReportParamAudit.class, null);

        Map<?, ?> converted = (Map<?, ?>) conversionService.convert(params,
                TypeDescriptor.valueOf(Map.class),
                new TypeDescriptor(type, null, null)
        );

        LocalDate from = (LocalDate) converted.get("from");
        LocalDate to = (LocalDate) converted.get("to");
        UUID uuid = (UUID) converted.get("user");

        Assertions.assertEquals(userUuid, uuid);

        Assertions.assertEquals(from, LocalDate.now());

        Assertions.assertEquals(to, LocalDate.now().plusDays(2));

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testPutReportWorks() {
        Map<String, String> init = new HashMap<>();
        init.put("user", UUID.randomUUID().toString());
        init.put("from", "2023-08-13");
        init.put("to", "2023-08-13");

        Assertions.assertDoesNotThrow(() -> reportService.putReportRequest(init, ReportType.JOURNAL_AUDIT));

    }


    @Test
    public void testPutReportWorks2() {
        Map<String, String> init = new HashMap<>();
        init.put("from", "2023-08-13");
        init.put("to", "2023-08-13");
        Assertions.assertDoesNotThrow(
                () -> reportService.putReportRequest(init, ReportType.JOURNAL_AUDIT));

    }

    @Test
    public void testPutReportThrows() {
        Map<String, String> init = new HashMap<>();
        init.put("user", null);
        init.put("from", "2023-08-13");
        init.put("to", "2023-08-13");
        StructuredException structuredException = Assertions.assertThrows(StructuredException.class,
                () -> reportService.putReportRequest(init, ReportType.JOURNAL_AUDIT));

    }


    @Test
    public void testPutReportThrows2() {
        Map<String, String> init = new HashMap<>();
        init.put("user", UUID.randomUUID().toString());
        init.put("from", "2023-09-13");
        init.put("to", "2023-08-13");
        GeneralException generalException = Assertions.assertThrows(GeneralException.class,
                () -> reportService.putReportRequest(init, ReportType.JOURNAL_AUDIT));

    }

    @Test
    public void findByTypeAndStatus() {
        List<Report> list = reportService.getReportsWithTypeAndStatus(ReportType.JOURNAL_AUDIT, ReportStatus.LOADED);
        Assertions.assertEquals(1, list.size());
    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testUpdateMethod() {
        UUID notReadyUuid = UUID.randomUUID();

        Report notReady = new Report(notReadyUuid);
        notReady.setType(ReportType.JOURNAL_AUDIT);
        notReady.setStatus(ReportStatus.PROGRESS);
        notReady.setParams(new HashMap<>());
        notReady.setDescription("some descr");

        reportRepository.saveAndFlush(notReady);

        reportService.setStatus(notReadyUuid, ReportStatus.DONE);

        Report report = reportRepository.findById(notReadyUuid).orElseThrow();

        Assertions.assertEquals(report.getStatus(), ReportStatus.DONE);

    }



    private String createFileWithAuditReports(List<AuditDTO> auditDTOList, UUID reportUuid) throws IOException {

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


    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }


    private static void fillProjectTableWithDefaultValues(IReportRepository reportRepository) {


        Report report = new Report(SAMPLE_REPORT_UUID);

        report.setDescription("some descr");
        report.setType(ReportType.JOURNAL_AUDIT);
        report.setStatus(ReportStatus.LOADED);

        Map<String, Object> params = new HashMap<>();

        UUID userUuid = UUID.randomUUID();

        params.put("user", userUuid);
        params.put("from", LocalDate.now());
        params.put("to", LocalDate.now().plusDays(2));

        report.setParams(params);

        reportRepository.save(report);

    }

}
