package org.example.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.core.dto.AuditDTO;
import org.example.core.dto.PageOfTypeDTO;
import org.example.dao.api.IAuditRepository;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test")
public class AuditRepositoryTest {

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

    @Test
    public void canGetDefaultValues() throws JsonProcessingException {

        List<Audit> all = repository.findAll();
        Assertions.assertEquals(6, all.size());

        UUID uuid = all.get(0).getUuid();

        Audit found = repository.findByUuid(uuid).orElseThrow();

        Assertions.assertEquals(
                all.get(0).getUuid(), found.getUuid()
        );

    }

    @Test
    public void canGetPages() throws JsonProcessingException {

        Page<Audit> page = repository.findAllByOrderByUuid(
                PageRequest.of(1, 2)
        );

        Assertions.assertEquals(3,page.getTotalPages());
        Assertions.assertEquals(6,page.getTotalElements());
        Assertions.assertEquals(2,page.getSize());

    }

    @Test
    public void canConvertToParametrizedPageWithoutException() {
        Page<Audit> page = repository.findAllByOrderByUuid(
                PageRequest.of(1, 2)
        );

        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                PageOfTypeDTO.class, AuditDTO.class
        );

        PageOfTypeDTO<AuditDTO> convert = (PageOfTypeDTO<AuditDTO>) conversionService.convert(
                page, TypeDescriptor.valueOf(PageImpl.class),
                new TypeDescriptor(resolvableType, null, null)
        );

        Assertions.assertNotNull(convert);
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
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.USER),
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.ADMIN),
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.USER),
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.ADMIN),
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.USER),
                new User(UUID.randomUUID(),"fake_mail","fake_fio", UserRole.USER)
        )
                .map(
                x -> new Audit(
                        UUID.randomUUID(),x,"some text", Type.USER, Type.USER.getId()
                ))
                .forEach(repository::save);

    }
}
