package org.example.service;

import com.google.common.base.CaseFormat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.core.dto.project.ProjectDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IProjectRepository;
import org.example.dao.api.ITaskRepository;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.ITaskService;
import org.example.service.api.IUserService;
import org.example.service.api.IUserServiceRequester;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;

import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
public class ProjectServiceImplTest {


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

    @Autowired
    private ConversionService conversionService;

    @SpyBean
    private IUserServiceRequester userServiceRequester;


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

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        Assertions.assertDoesNotThrow(() -> projectService.save(projectCreateDTO));

        projectCreateDTO.setManager(new UserDTO());

        ConstraintViolationException exception = Assertions.assertThrows(ConstraintViolationException.class,
                () -> projectService.save(projectCreateDTO));

        Map<String, String> res1 = constraintViolationExceptionParser(exception);

        Assertions.assertEquals(1,res1.size());
        Assertions.assertEquals(res1.get("manager"), "uuid must not be null");

        Set<UserDTO> staff = new HashSet<>();
        staff.addAll(List.of(
                new UserDTO(),
                new UserDTO(UUID.randomUUID()),
                new UserDTO(UUID.randomUUID()))
        );

        projectCreateDTO.setStaff(staff);

        ConstraintViolationException exception2 = Assertions.assertThrows(ConstraintViolationException.class,
                () -> projectService.save(projectCreateDTO));

        res1 = constraintViolationExceptionParser(exception2);

        Assertions.assertEquals(res1.size(), 2);
        Assertions.assertEquals(res1.get("manager"), "uuid must not be null");
        Assertions.assertEquals(res1.get("staff"), "uuid is not specified");

    }

    @Test
    public void getPageWorks() {
        Page<Project> page = projectService.getPage(0, 2, true);

            ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                    PageOfTypeDTO.class, ProjectDTO.class
            );

            Assertions.assertDoesNotThrow(()->{
                Object convert = conversionService.convert(
                        page, TypeDescriptor.valueOf(PageImpl.class),
                        new TypeDescriptor(resolvableType, null, null)
                );

                Assertions.assertNotNull(convert);

            });

    }

    @Test
    public void getPageThrows() {
        StructuredException exception = Assertions.assertThrows(
                StructuredException.class,
                () -> projectService.getPage(-1, 0, true)
        );

        Assertions.assertEquals(2, exception.getSize());

    }




    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void validationOnServiceWorks() {
        UserDTO manager = new UserDTO();

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("dfdf");
        projectCreateDTO.setManager(manager);

        Assertions.assertThrows(ConstraintViolationException.class,
                () -> projectService.save(projectCreateDTO));

        manager.setUuid(UUID.randomUUID());

        doReturn(manager).when(userServiceRequester).getUser(manager.getUuid());

        Assertions.assertThrows(GeneralException.class,
                () -> projectService.save(projectCreateDTO));

        manager.setRole(UserRole.USER);

        Assertions.assertThrows(StructuredException.class,
                () -> projectService.save(projectCreateDTO));


        projectCreateDTO.setStaff(Set.of(new UserDTO(), new UserDTO(UUID.randomUUID())));

        Assertions.assertThrows(ConstraintViolationException.class,
                () -> projectService.save(projectCreateDTO));


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveWithManagerWorksFine() {

        UUID managerUUID = UUID.randomUUID();

        UserDTO manager = new UserDTO(managerUUID);
        manager.setRole(UserRole.MANAGER);

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("dfdf");
        projectCreateDTO.setManager(manager);

        doReturn(manager).when(userServiceRequester).getUser(managerUUID);

        Project save = projectService.save(projectCreateDTO);

        Mockito.verify(userServiceRequester, Mockito.times(1)).getUser(Mockito.any());

        Assertions.assertTrue(userRepository.existsById(managerUUID));

        Assertions.assertNotNull(save);


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveWorksFine() {

        UUID managerUUID = UUID.randomUUID();

        UUID workerUUid1 = UUID.randomUUID();
        UUID workerUUid2 = UUID.randomUUID();
        UUID workerUUid3 = UUID.randomUUID();

        UserDTO worker1 = new UserDTO(workerUUid1);
        UserDTO worker2 = new UserDTO(workerUUid2);
        UserDTO worker3 = new UserDTO(workerUUid3);

        Set <UserDTO> staff = new HashSet<>();
        staff.add(worker1); staff.add(worker2);
        staff.add(worker3);

        UserDTO manager = new UserDTO(managerUUID);
        manager.setRole(UserRole.MANAGER);

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("some_name");
        projectCreateDTO.setManager(manager);
        projectCreateDTO.setStaff(staff);

        doReturn(manager).when(userServiceRequester).getUser(managerUUID);
        doReturn(staff).when(userServiceRequester).getSetOfUserDTO(Mockito.any());

        Project save = projectService.save(projectCreateDTO);

        Mockito.verify(userServiceRequester, Mockito.times(1)).getUser(Mockito.any());

        Assertions.assertTrue(userRepository.existsById(managerUUID));

        Assertions.assertNotNull(save);

        Assertions.assertEquals(3, save.getStaff().size());
        Assertions.assertNotNull(save.getManager());


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
