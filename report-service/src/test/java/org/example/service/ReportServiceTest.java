package org.example.service;

import org.example.core.dto.report.ReportDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.dao.api.IReportRepository;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        Assertions.assertDoesNotThrow(()-> conversionService.convert(report, ReportDTO.class));

    }


    @Test
    public void testConversionFromMapToMapWorks() {

        Map<String, String> params = new HashMap<>();

        UUID userUuid = UUID.randomUUID();

        params.put("user", userUuid.toString());
        params.put("from", LocalDate.now().toString());
        params.put("to", LocalDate.now().plusDays(2).toString());

        ResolvableType type = ResolvableType.forClassWithGenerics(Map.class, ReportParamAudit.class, null);

        Map<?,?> converted = (Map<?, ?>) conversionService.convert(params,
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
