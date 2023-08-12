package org.example.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.example.dao.api.IProjectRepository;
import org.example.dao.api.ITaskRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.example.dao.entities.user.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TaskRepositoryDaoTest {

    private static final UUID USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS = UUID.randomUUID();
    private static final UUID USER_UUID_WORKS_ON_SEPARATE_TASK = UUID.randomUUID();

    private static final UUID INIT_PROJECT_UUID = UUID.randomUUID();
    private static final UUID PROJECT_2_UUID = UUID.randomUUID();
    private static final UUID PROJECT_3_UUID = UUID.randomUUID();
    private static final UUID PROJECT_4_UUID = UUID.randomUUID();

    private static final UUID INIT_TASK_UUID = UUID.randomUUID();
    private static final UUID TASK_UUID_CLOSE_IN_PROJECT_2 = UUID.randomUUID();
    private static final UUID TASK_UUID_IN_WORK_IN_PROJECT_3 = UUID.randomUUID();


    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITaskRepository taskRepository;
    @Autowired
    private IProjectRepository projectRepository;


    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource,
                                             @Autowired ITaskRepository taskRepository,
                                             @Autowired IUserRepository userRepository,
                                             @Autowired IProjectRepository projectRepository) {
        clearAndInitSchema(dataSource);
        fillDataBaseWithDefaultValues(taskRepository, userRepository, projectRepository);
    }

    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }

    private static void fillDataBaseWithDefaultValues(ITaskRepository taskRepository,
                                                      IUserRepository userRepository, IProjectRepository projectRepository) {
        persistDefaultStaffAndProject(userRepository, projectRepository, taskRepository);

    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, taskRepository, userRepository, projectRepository);
        }

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void createNewTask() {

        Task task = new Task(UUID.randomUUID());

        task.setProject(projectRepository.findAll().get(0));
        task.setTitle("Init task title");
        task.setDescription("Init task description");
        task.setStatus(TaskStatus.IN_WORK);
        task.setImplementer(userRepository.findAll().get(0));
        taskRepository.save(task);


    }


    @Test
    public void taskSpecificationWorksOnStatusFilter() {

        List<Project> allProjects = projectRepository.findAll();

        List<Task> all = taskRepository.findAll(getTaskSpecificationOnFilters(allProjects, null, List.of(TaskStatus.IN_WORK)));

        Assertions.assertEquals(2, all.size());


    }

    @Test
    public void taskSpecificationWorksWithoutStatusFilter() {

        List<Project> allProjects = projectRepository.findAll();

        List<Task> all = taskRepository.findAll(getTaskSpecificationOnFilters(allProjects, null, null));

        Assertions.assertEquals(3, all.size());


    }

    @Test
    public void taskSpecificationWorksOnProjectsAndImplementersFilter() {


        Project init_project = projectRepository.findById(INIT_PROJECT_UUID).orElseThrow();

        Project project3 = projectRepository.findById(PROJECT_3_UUID).orElseThrow();


        List<Task> all = taskRepository.findAll(getTaskSpecificationOnFilters(
                List.of(init_project, project3), List.of(USER_UUID_WORKS_ON_SEPARATE_TASK), null)
        );

        Assertions.assertEquals(1, all.size());


    }

    private static void persistDefaultStaffAndProject(IUserRepository userRepository, IProjectRepository projectRepository, ITaskRepository taskRepository) {

        User userWorksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        User userWorksOnSeparateTaskInProject3 = new User(USER_UUID_WORKS_ON_SEPARATE_TASK);


        List<User> users = userRepository.saveAllAndFlush(
                List.of(
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID()),
                        new User(UUID.randomUUID())
                )
        );

        userRepository.saveAndFlush(userWorksIn3Projects);
        userRepository.saveAndFlush(userWorksOnSeparateTaskInProject3);


        Project init_project = new Project(INIT_PROJECT_UUID);

        init_project.setManager(userWorksIn3Projects);
        init_project.setStaff(Set.of(users.get(0), users.get(1), users.get(2)));
        init_project.setStatus(ProjectStatus.ACTIVE);
        init_project.setName("Init project");
        init_project.setDescription("Init project with 3 staff");

        projectRepository.saveAndFlush(init_project);

        Project project1 = new Project(PROJECT_2_UUID);

        project1.setManager(users.get(5));
        project1.setStaff(Set.of(users.get(6), users.get(7), userWorksIn3Projects));
        project1.setStatus(ProjectStatus.ARCHIVED);
        project1.setName("Init project2");
        project1.setDescription("Init project2 with 3 staff archived");

        projectRepository.saveAndFlush(project1);


        Project project2 = new Project(PROJECT_3_UUID);

        project2.setManager(userWorksIn3Projects);
        project2.setStatus(ProjectStatus.ACTIVE);
        project2.setName("Init project3");
        project2.setDescription("Init project3 with 0 staff");
        project2.setStaff(Set.of(userWorksOnSeparateTaskInProject3));

        projectRepository.saveAndFlush(project2);

        Project project3 = new Project(PROJECT_4_UUID);

        project3.setManager(users.get(0));
        project1.setStaff(Set.of(users.get(6), users.get(7), users.get(2)));
        project3.setStatus(ProjectStatus.ACTIVE);
        project3.setName("Init project4");
        project3.setDescription("Init project4 with 3 staff");

        projectRepository.saveAndFlush(project3);


        Task initTask = new Task();
        initTask.setUuid(INIT_TASK_UUID);
        initTask.setTitle("init_title");
        initTask.setImplementer(userWorksIn3Projects);
        initTask.setStatus(TaskStatus.IN_WORK);
        initTask.setProject(init_project);

        taskRepository.saveAndFlush(initTask);

        Task task2 = new Task();
        task2.setUuid(TASK_UUID_CLOSE_IN_PROJECT_2);
        task2.setTitle("task_close_in_project_2");
        task2.setImplementer(userWorksIn3Projects);
        task2.setStatus(TaskStatus.CLOSE);
        task2.setProject(project1);

        taskRepository.saveAndFlush(task2);


        Task task3 = new Task();
        task3.setUuid(TASK_UUID_IN_WORK_IN_PROJECT_3);
        task3.setTitle("task_in_work_in_project_3");
        task3.setImplementer(userWorksOnSeparateTaskInProject3);
        task3.setStatus(TaskStatus.IN_WORK);
        task3.setProject(project2);

        taskRepository.saveAndFlush(task3);

    }

    private static Specification<Task> getTaskSpecificationOnFilters(List<Project> projectsToFilter, List<UUID> implementersUuids, List<TaskStatus> taskStatuses) {
        return (root, query, builder) -> {

            Path<Object> project = root.get("project");
            CriteriaBuilder.In<Object> inProject = builder.in(project);
            Predicate res = inProject.value(projectsToFilter);

            if (taskStatuses != null && taskStatuses.size() != 0) {

                Path<Object> status = root.get("status");
                CriteriaBuilder.In<Object> inStatus = builder.in(status);
                Predicate inListOfStatuses = inStatus.value(taskStatuses);
                res = builder.and(res, inListOfStatuses);

            }

            if (implementersUuids != null && implementersUuids.size() != 0) {

                Path<Object> implementerUuid = root.get("implementer").get("uuid");
                CriteriaBuilder.In<Object> inImplementer = builder.in(implementerUuid);
                Predicate inListOfImplementers = inImplementer.value(implementersUuids);
                res = builder.and(res, inListOfImplementers);

            }

            return res;

        };
    }


}
