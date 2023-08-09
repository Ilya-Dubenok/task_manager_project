package org.example.dao;

import jakarta.persistence.criteria.Predicate;
import org.example.dao.api.IProjectRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.user.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;
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
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IProjectRepository projectRepository,
                                             @Autowired IUserRepository userRepository) {
        clearAndInitSchema(dataSource);
        fillProjectTableWithDefaultValues(projectRepository, userRepository);
    }

    private static void fillProjectTableWithDefaultValues(IProjectRepository projectRepository,
                                                          IUserRepository userRepository) {
        List<User> users = Stream.generate(
                () -> new User(UUID.randomUUID())
        ).limit(50).toList();
        userRepository.saveAllAndFlush(users);

        for (int i = 0; i < 5; i++) {

            User manager = userRepository.save(new User(UUID.randomUUID()));


            Project project = new Project(UUID.randomUUID());

            Set<User> theMightyTen = new HashSet<>(users.subList(i * 10, (i + 1) * 10));
            project.setStaff(theMightyTen);
            project.setManager(manager);

            projectRepository.save(project);

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

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, projectRepository, userRepository);
        }

    }


    @Test
    public void testPageSpecificationGeneral() {
        User employed = userRepository.findAll().get(0);
        Page<Project> page = projectRepository.findAll(
                getSpecificationOfUserIsInProjectAndShowArchivedIs(employed, false),
                PageRequest.of(0, 10));
        Assertions.assertEquals(1, page.getTotalElements());

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testPageSpecificationNotShowArchived() {

        User worksInArchivedOnly = new User(UUID.randomUUID());
        userRepository.save(worksInArchivedOnly);

        Project project = projectRepository.findAll().get(0);
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setManager(worksInArchivedOnly);

        projectRepository.save(project);

        Page<Project> page = projectRepository.findAll(
                getSpecificationOfUserIsInProjectAndShowArchivedIs(worksInArchivedOnly, false),
                PageRequest.of(0, 10)
        );

        Assertions.assertEquals(0, page.getTotalElements());

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testPageSpecificationShowArchived() {

        User worksInArchivedOnly = new User(UUID.randomUUID());
        userRepository.save(worksInArchivedOnly);

        Project project = projectRepository.findAll().get(0);
        project.setStatus(ProjectStatus.ARCHIVED);
        project.setStaff(Set.of(worksInArchivedOnly));

        projectRepository.save(project);

        Page<Project> page = projectRepository.findAll(
                getSpecificationOfUserIsInProjectAndShowArchivedIs(worksInArchivedOnly, true),
                PageRequest.of(0, 10)
        );

        Assertions.assertEquals(1, page.getTotalElements());

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void testPageSpecificationShowArchivedAndNot() {

        User worksInBothArchivedAndNot = new User(UUID.randomUUID());
        userRepository.save(worksInBothArchivedAndNot);

        List<Project> projects = projectRepository.findAll();

        Project archived = projects.get(0);
        archived.setStatus(ProjectStatus.ARCHIVED);
        archived.setStaff(Set.of(worksInBothArchivedAndNot));

        projectRepository.save(archived);

        Project notArchived = projects.get(1);
        notArchived.setManager(worksInBothArchivedAndNot);
        notArchived.setStatus(ProjectStatus.ACTIVE);

        projectRepository.save(notArchived);

        Page<Project> page = projectRepository.findAll(
                getSpecificationOfUserIsInProjectAndShowArchivedIs(worksInBothArchivedAndNot, true),
                PageRequest.of(0, 10)
        );

        Assertions.assertEquals(2, page.getTotalElements());

    }


    public static Specification<Project> getSpecificationOfUserIsInProjectAndShowArchivedIs(User user, Boolean showArchived) {
        return (root, query, builder) -> {

            Predicate isManager = builder.equal(root.get("manager"), user);
            Predicate isInStuff = builder.isMember(user, root.get("staff"));

            Predicate res = builder.or(isManager, isInStuff);

            if (showArchived) {
                return res;
            }

            Predicate isProjectArchived = builder.or(
                    builder.notEqual(root.get("status"), ProjectStatus.ARCHIVED),
                    builder.isNull(root.get("status"))
            );

            res = builder.and(res, isProjectArchived);

            return res;
        };
    }


}
