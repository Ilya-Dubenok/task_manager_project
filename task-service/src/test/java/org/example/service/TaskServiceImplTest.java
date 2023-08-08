package org.example.service;

import com.google.common.base.CaseFormat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.api.IProjectRepository;
import org.example.dao.api.ITaskRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.ITaskService;
import org.example.service.api.IUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceImplTest {



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
    private IUserService userService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private ITaskService taskService;


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
        persistDefaultStaffAndProject(userRepository, projectRepository);

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


    private static void persistDefaultStaffAndProject(IUserRepository userRepository, IProjectRepository projectRepository) {

        List<User> users = userRepository.saveAll(
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


        Project project = new Project(UUID.randomUUID());

        project.setManager(users.get(0));
        project.setStaff(Set.of(users.get(1), users.get(2), users.get(3)));
        project.setStatus(ProjectStatus.ACTIVE);
        project.setName("Init project");
        project.setDescription("Init project with 3 staff");

        projectRepository.save(project);

    }

}
