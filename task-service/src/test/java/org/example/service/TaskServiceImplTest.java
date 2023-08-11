package org.example.service;

import com.google.common.base.CaseFormat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.exception.AuthenticationFailedException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IProjectRepository;
import org.example.dao.api.ITaskRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.ITaskService;
import org.example.service.api.IUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceImplTest {

    private static final UUID USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS = UUID.randomUUID();

    private static final UUID INIT_PROJECT_UUID = UUID.randomUUID();
    private static final UUID PROJECT_2_UUID = UUID.randomUUID();
    private static final UUID PROJECT_3_UUID = UUID.randomUUID();
    private static final UUID PROJECT_4_UUID = UUID.randomUUID();

    private static final UUID INIT_TASK_UUID = UUID.randomUUID();

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
    private IProjectService projectService;

    @Autowired
    private ITaskService taskService;

    @SpyBean
    private IUserService userService;





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
    public void validationOnFieldsWithCustomAnnotations() {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setProject(new ProjectUuidDTO());
        taskCreateDTO.setImplementer(new UserDTO());

        ConstraintViolationException exception1 = Assertions.assertThrows(ConstraintViolationException.class,
                () -> taskService.save(taskCreateDTO));

        Map<String, String> res2 = constraintViolationExceptionParser(exception1);
        Assertions.assertEquals(3, res2.size());
        Assertions.assertEquals(res2.get("project"), "uuid must not be null");
        Assertions.assertEquals(res2.get("implementer"), "uuid must not be null");

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveMethodWorks() {

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
                new ProjectUuidDTO(PROJECT_2_UUID), "mustached_boss", "put_off_your_clothes_and_work",
                TaskStatus.IN_WORK, new UserDTO(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS)
        );

        User inProject = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        doReturn(inProject).when(userService).findUserInCurrentContext();

        Task save = taskService.save(taskCreateDTO);

        Assertions.assertNotNull(save);

    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveThrowsWhenImproperRequester() {

        UserDTO notInProjectRequested = new UserDTO(UUID.randomUUID());

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
                new ProjectUuidDTO(PROJECT_2_UUID), "mustached_boss", "put_off_your_clothes_and_work",
                TaskStatus.IN_WORK, notInProjectRequested
        );

        doReturn(new User(notInProjectRequested.getUuid())).when(userService).findUserInCurrentContext();

        Assertions.assertThrows(AuthenticationFailedException.class, ()->taskService.save(taskCreateDTO));


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveThrowsWhenImproperImplementer() {

        UserDTO fakeImplementer = new UserDTO(UUID.randomUUID());

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
                new ProjectUuidDTO(PROJECT_2_UUID), "mustached_boss", "put_off_your_clothes_and_work",
                TaskStatus.IN_WORK, fakeImplementer
        );

        doReturn(new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS)).when(userService).findUserInCurrentContext();

        Assertions.assertThrows(StructuredException.class, ()->taskService.save(taskCreateDTO));


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateWorksWhenAllIsFine() {

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO(
                new ProjectUuidDTO(PROJECT_2_UUID), "mustached_boss", "put_off_your_clothes_and_work",
                TaskStatus.IN_WORK, new UserDTO(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS)
        );

        User inProject = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        doReturn(inProject).when(userService).findUserInCurrentContext();

        Task target = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        LocalDateTime dtUpdate = target.getDtUpdate();

        Task update = taskService.update(INIT_TASK_UUID, dtUpdate, taskCreateDTO);

        Assertions.assertNotEquals(target.getTitle(), update.getTitle());

        Assertions.assertNotEquals(target.getDtUpdate(), update.getDtUpdate());


    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateStatusWorksWhenUserIsInProject() {

        Task target = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        LocalDateTime dtUpdate = target.getDtUpdate();

        User inProject = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        doReturn(inProject).when(userService).findUserInCurrentContext();

        Task updated = taskService.updateStatus(INIT_TASK_UUID, dtUpdate, null);

        Assertions.assertNull(updated.getStatus());

    }

    @Test
    public void updateStatusFailsWhenUserIsNotInProject() {

        Task target = taskRepository.findById(INIT_TASK_UUID).orElseThrow();

        LocalDateTime dtUpdate = target.getDtUpdate();

        User notInProject = new User(UUID.randomUUID());

        doReturn(notInProject).when(userService).findUserInCurrentContext();

        Assertions.assertThrows(AuthenticationFailedException.class,
                ()->taskService.updateStatus(INIT_TASK_UUID, dtUpdate, null));

    }


    @Test
    public void nullWhenUserIsNotInProject() {


        User notInProject = new User(UUID.randomUUID());

        doReturn(notInProject).when(userService).findUserInCurrentContext();

        Task res = taskService.findByUUID(INIT_TASK_UUID);

        Assertions.assertNull(res);

    }

    @Test
    public void taskReturnedWhenUserIsInProject() {


        User inProject = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        doReturn(inProject).when(userService).findUserInCurrentContext();

        Task res = taskService.findByUUID(INIT_TASK_UUID);

        Assertions.assertNotNull(res);

    }

    @Test
    public void pageWithNothingReturnedWhenForbiddenUuidIsPassed() {

        User inProject = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        doReturn(inProject).when(userService).findUserInCurrentContext();

        Page<Task> page = taskService.getPageWithFilters(0, 20, List.of(PROJECT_4_UUID), null, null);

        Assertions.assertEquals(0, page.getTotalPages());

        Assertions.assertEquals(0, page.getTotalElements());
    }


    private Map<String, String> constraintViolationExceptionParser(ConstraintViolationException e) {

        Iterator<ConstraintViolation<?>> iterator = e.getConstraintViolations().iterator();
        Map<String, String> map = new HashMap<>();

        while (iterator.hasNext()) {
            ConstraintViolation<?> constraintViolation = iterator.next();
            String propName = parseForPropNameInSnakeCase(constraintViolation);
            String message = constraintViolation.getMessage();
            map.put(propName, message);
        }

        return map;

    }


    private String parseForPropNameInSnakeCase(ConstraintViolation<?> next) {

        Path propertyPath = next.getPropertyPath();
        Iterator<Path.Node> iterator = propertyPath.iterator();
        Path.Node node = null;

        while (iterator.hasNext()) {
            node = iterator.next();

        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getName());
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


    }

}
