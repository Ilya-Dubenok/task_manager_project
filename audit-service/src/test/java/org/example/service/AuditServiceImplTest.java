package org.example.service;

import jakarta.validation.ConstraintViolationException;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IAuditRepository;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
import org.example.core.dto.user.UserAuditDTO;
import org.example.core.dto.user.UserRole;
import org.example.service.api.IAuditService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test")
public class AuditServiceImplTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @Autowired
    DataSource dataSource;

    @Autowired
    IAuditRepository repository;


    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private IAuditService auditService;

    @Test
    public void saveWithNotValidAuditCreateDTO() {

        AuditCreateDTO createDTO = new AuditCreateDTO();

        createDTO.setText("");

        createDTO.setType(Type.USER);

        ConstraintViolationException exception = Assertions.assertThrows(
                ConstraintViolationException.class,
                () -> auditService.save(createDTO)
        );

        Assertions.assertEquals(
                3, exception.getConstraintViolations().size()
        );
    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveWithValidAuditCreateDTO() {

        AuditCreateDTO createDTO = new AuditCreateDTO();

        createDTO.setText("some");

        createDTO.setUser(
                new UserAuditDTO(UUID.randomUUID(), "mail", "fio", UserRole.USER)
        );

        createDTO.setType(Type.USER);

        createDTO.setId(UUID.randomUUID().toString());

        Assertions.assertDoesNotThrow(
                () -> auditService.save(createDTO)
        );

    }

    @Test
    public void getPagewithInvalidValuesFails() {

        StructuredException exception = Assertions.assertThrows(
                StructuredException.class,
                () -> auditService.getPageOfAudit(
                        -1, 0
                )
        );

        Assertions.assertEquals(2, exception.getSize());

    }


    @Test
    public void getPageValidWorks() {

        Page<Audit> pageOfAudit = auditService.getPageOfAudit(1, 2);

        Assertions.assertNotNull(pageOfAudit);

        Assertions.assertEquals(3, pageOfAudit.getTotalPages());


    }

    @Test
    public void getAuditByIdWorks() {

        UUID uuid = repository.findAll().get(0).getUuid();

        Audit audit = auditService.getAuditById(uuid);

        Assertions.assertNotNull(audit);

        Assertions.assertEquals(uuid, audit.getUuid());

    }


    @Test
    public void getAuditByIdFailsWhenNotFound() {

        UUID uuid = UUID.randomUUID();

        Assertions.assertThrows(
                GeneralException.class,
                () -> auditService.getAuditById(uuid)
        );


    }


    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IAuditRepository repository) {
        clearAndInitSchema(dataSource);
        fillUserTableWithDefaultValues(repository);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, repository);
        }

    }


    private static void clearAndInitSchema(DataSource dataSource) {

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }


    private static void fillUserTableWithDefaultValues(IAuditRepository repository) {

        Stream.of(
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.ADMIN),
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.ADMIN),
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.USER),
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.USER),
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.USER),
                        new UserAuditDTO(UUID.randomUUID(), "fake_mail", "fake_fio", UserRole.USER)
                )
                .map(
                        x -> new Audit(
                                UUID.randomUUID(), x, "some text", Type.USER, x.getUuid().toString()
                        ))
                .forEach(repository::save);

    }

}
