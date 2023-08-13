package org.example.dao;


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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ReportRepositoryTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


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
    public void testPageSpecificationGeneral() {

        Report report = new Report(UUID.randomUUID());

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

        Report persisted = reportRepository.findAll().get(0);

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


    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }


    private static void fillProjectTableWithDefaultValues(IReportRepository reportRepository) {


    }


}
