package org.example.dao;


import org.example.core.dto.report.ReportDTO;
import org.example.dao.api.IReportRepository;
import org.example.dao.entities.Report;
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
import java.util.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ReportRepositoryTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    private static final UUID SAMPLE_REPORT_UUID = UUID.randomUUID();


    @Autowired
    private DataSource dataSource;

    @Autowired
    private IReportRepository reportRepository;

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
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testPersistenceWorks() {

        UUID reportUuid = UUID.randomUUID();
        Report report = new Report(reportUuid);

        report.setDescription("some descr");
        report.setType(ReportType.JOURNAL_AUDIT);
        report.setStatus(ReportStatus.LOADED);

        Map<String, Object> params = new HashMap<>();

        UUID userUuid = UUID.randomUUID();

        params.put("user", userUuid);
        params.put("from", LocalDate.now());
        params.put("to", LocalDate.now().plusDays(2));

        report.setParams(params);

        reportRepository.saveAndFlush(report);

        Report persisted = reportRepository.findById(reportUuid).orElseThrow();

        Map<String, Object> params1 = persisted.getParams();

        UUID user =   UUID.fromString((String) params1.get("user"));

        Assertions.assertEquals(userUuid, user);

        List<?> from = (List<?>) params1.get("from");

        LocalDate persistedFrom = LocalDate.of((int) from.get(0), (int) from.get(1), (int) from.get(2));

        Assertions.assertEquals(LocalDate.now(), persistedFrom);

        List<?> to = (List<?>) params1.get("to");

        LocalDate persistedTo = LocalDate.of((int) to.get(0), (int) to.get(1), (int) to.get(2));

        Assertions.assertEquals(LocalDate.now().plusDays(2), persistedTo);


    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testFindIsReportAvalableWorks() {

        UUID readyUuid = UUID.randomUUID();

        Report ready = new Report(readyUuid);
        ready.setType(ReportType.JOURNAL_AUDIT);
        ready.setStatus(ReportStatus.DONE);
        ready.setParams(new HashMap<>());
        ready.setDescription("some descr");

        reportRepository.saveAndFlush(ready);


        boolean present = reportRepository.findByUuidAndStatusIs(readyUuid, ReportStatus.DONE).isPresent();
        Assertions.assertTrue(present);


        UUID notReadyUuid = UUID.randomUUID();

        Report notReady = new Report(notReadyUuid);
        notReady.setType(ReportType.JOURNAL_AUDIT);
        notReady.setStatus(ReportStatus.PROGRESS);
        notReady.setParams(new HashMap<>());
        notReady.setDescription("some descr");

        reportRepository.saveAndFlush(notReady);


        present = reportRepository.findByUuidAndStatusIs(notReadyUuid, ReportStatus.DONE).isPresent();
        Assertions.assertFalse(present);
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
