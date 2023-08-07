package org.example.dao;

import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
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
import java.util.Set;
import java.util.UUID;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IUserRepository repository;



    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IUserRepository userRepository) {
        clearAndInitSchema(dataSource);
        fillUserTableWithDefaultValues(userRepository);
    }

    private static void fillUserTableWithDefaultValues(IUserRepository userRepository) {
    }

    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, repository);
        }

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveOneUser() {

        UUID uuid = UUID.randomUUID();
        repository.save(new User(uuid));
        User user = repository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Assertions.assertEquals(
                uuid, user.getUuid()
        );


    }


}
