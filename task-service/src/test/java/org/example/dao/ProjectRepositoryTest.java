package org.example.dao;

import org.example.dao.api.IProjectRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ProjectRepositoryTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IProjectRepository projectRepository;

    @Autowired
    private IUserRepository userRepository;


    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IProjectRepository projectRepository) {
        clearAndInitSchema(dataSource);
        fillProjectTableWithDefaultValues(projectRepository);
    }

    private static void fillProjectTableWithDefaultValues(IProjectRepository projectRepository) {
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
            initWithDefaultValues(dataSource, projectRepository);
        }

    }

    @Test
    public void createdUsersAreNotDeleted() {

        UUID managerUUID = UUID.randomUUID();
        User manager = new User(managerUUID);
        Set<User> staff = Stream.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        )
                .map(User::new)
                .collect(Collectors.toSet());

        userRepository.saveAll(staff);
        userRepository.save(manager);


        Project project = new Project(UUID.randomUUID());
        project.setManager(new User(managerUUID));
        project.setStaff(staff);

        projectRepository.save(project);

        Project saved = projectRepository.findAll().get(0);

        Assertions.assertEquals(project.getUuid(), saved.getUuid());

        projectRepository.deleteAll();

        List<User> users = userRepository.findAll();

        Assertions.assertEquals(4, users.size());



    }


}
