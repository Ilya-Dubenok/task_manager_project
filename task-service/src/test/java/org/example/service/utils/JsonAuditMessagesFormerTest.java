package org.example.service.utils;


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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class JsonAuditMessagesFormerTest {

    private static final UUID USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS = UUID.randomUUID();

    private static final UUID INIT_PROJECT_UUID = UUID.randomUUID();
    private static final UUID PROJECT_2_UUID = UUID.randomUUID();
    private static final UUID PROJECT_3_UUID = UUID.randomUUID();
    private static final UUID PROJECT_4_UUID = UUID.randomUUID();

    private static final UUID INIT_TASK_UUID = UUID.randomUUID();
    private static final UUID SECOND_TASK_UUID = UUID.randomUUID();

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @Autowired
    private DataSource dataSource;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITaskRepository taskRepository;

    @Autowired
    private IProjectRepository projectRepository;

    @Autowired
    private JsonAuditMessagesFormer jsonAuditMessagesFormer;





    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource,
                                             @Autowired ITaskRepository taskRepository,
                                             @Autowired IUserRepository userRepository,
                                             @Autowired IProjectRepository projectRepository) {
        clearAndInitSchema(dataSource);
        fillDataBaseWithDefaultValues(dataSource, taskRepository, userRepository, projectRepository);
    }

    private static void clearAndInitSchema(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }

    private static void fillDataBaseWithDefaultValues(DataSource dataSource, ITaskRepository taskRepository,
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
    public void taskCreatedMessageFormed() {

        Task task = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectCreatedAuditMessage(task));

    }

    @Test
    public void projectCreatedMessageFormed() {

        Project project = projectRepository.findById(PROJECT_2_UUID).orElseThrow();

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectCreatedAuditMessage(project));

    }

    @Test
    public void taskUpdatedMessageFormed() {

        Task task1 = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        task1.setProject(null);

        Task task2 = taskRepository.findById(SECOND_TASK_UUID).orElseThrow();

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(task1, task2));

    }

    @Test
    public void taskUpdatedMessageFormed2() {

        Task task1 = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        Task task2 = taskRepository.findById(SECOND_TASK_UUID).orElseThrow();

        task2.setProject(null);

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(task1, task2));

    }

    @Test
    public void taskUpdatedMessageFormed3() {

        Task task1 = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        Task task2 = taskRepository.findById(SECOND_TASK_UUID).orElseThrow();

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(task1, task2));

    }

    @Test
    public void taskUpdatedMessageFormed4() {

        Task task1 = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        Task task2 = taskRepository.findById(SECOND_TASK_UUID).orElseThrow();

        task2.setProject(new Project(UUID.randomUUID()));

        task2.setImplementer(null);

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(task1, task2));

    }

    @Test
    public void projectUpdatedMessageFormed1() {

        Project project1 = projectRepository.findById(INIT_PROJECT_UUID).orElseThrow();
        Project project2 = projectRepository.findById(PROJECT_2_UUID).orElseThrow();

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(project1, project2));

    }

    @Test
    public void projectUpdatedMessageFormed2() {

        Project project1 = projectRepository.findById(INIT_PROJECT_UUID).orElseThrow();
        Project project2 = projectRepository.findById(INIT_PROJECT_UUID).orElseThrow();

        project2.setName(null);

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formObjectUpdatedAuditMessage(project1, project2));

    }



    private static void persistDefaultStaffAndProject(IUserRepository userRepository, IProjectRepository projectRepository, ITaskRepository taskRepository) {

        User userWorksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

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
        initTask.setImplementer(new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS));
        initTask.setStatus(TaskStatus.IN_WORK);
        initTask.setProject(init_project);

        taskRepository.saveAndFlush(initTask);


        Task secondTask = new Task();
        secondTask.setUuid(SECOND_TASK_UUID);
        secondTask.setTitle("second_title");
        secondTask.setImplementer(new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS));
        secondTask.setStatus(TaskStatus.IN_WORK);
        secondTask.setProject(init_project);

        taskRepository.saveAndFlush(secondTask);



    }

}
