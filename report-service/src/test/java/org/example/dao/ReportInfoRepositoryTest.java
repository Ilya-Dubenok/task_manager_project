package org.example.dao;


import org.example.dao.api.IReportInfoRepository;
import org.example.dao.api.IReportRepository;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportInfo;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ReportInfoRepositoryTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    private static final UUID SAMPLE_REPORT_UUID = UUID.randomUUID();


    @Autowired
    private DataSource dataSource;

    @Autowired
    private IReportRepository reportRepository;

    @Autowired
    private IReportInfoRepository reportInfoRepository;

    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource,
                                             @Autowired IReportRepository reportRepository,
                                             @Autowired IReportInfoRepository reportInfoRepository) {

        clearAndInitSchema(dataSource);
        fillReportInfoWithDefaultValues(reportInfoRepository, reportRepository);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, reportRepository, reportInfoRepository);
        }

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveReportInfoWithWrongUuid() {

        Assertions.assertThrows(Exception.class, () ->
                reportInfoRepository.save(new ReportInfo(UUID.randomUUID(), "fileName", "bucketName"))
        );


    }



    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveValidReportInfo() {
        Assertions.assertDoesNotThrow(() ->
                reportInfoRepository.save(new ReportInfo(SAMPLE_REPORT_UUID, "fileName", "bucketName"))
        );
    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveAndFindReportInfoWithValidUuid() {

        reportInfoRepository.save(new ReportInfo(SAMPLE_REPORT_UUID, "fileName", "bucketName"));

        Assertions.assertTrue(
                reportInfoRepository.findByReportUuid(SAMPLE_REPORT_UUID).isPresent()
        );
    }

    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }


    private static void fillReportInfoWithDefaultValues(IReportInfoRepository reportInfoRepository, IReportRepository reportRepository) {


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
