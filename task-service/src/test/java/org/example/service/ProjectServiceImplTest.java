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
import org.example.core.exception.AuthenticationFailedException;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
public class ProjectServiceImplTest {

    private static final UUID USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS = UUID.randomUUID();
    private static final UUID INIT_PROJECT_UUID = UUID.randomUUID();
    private static final UUID PROJECT2_UUID = UUID.randomUUID();
    private static final UUID PROJECT3_UUID = UUID.randomUUID();
    private static final UUID PROJECT4_UUID = UUID.randomUUID();

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITaskRepository taskRepository;

    @Autowired
    private IProjectRepository projectRepository;

    @SpyBean
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

    private static void fillDataBaseWithDefaultValues(DataSource dataSource,
                                                      ITaskRepository taskRepository,
                                                      IUserRepository userRepository,
                                                      IProjectRepository projectRepository) {

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

        Set<UserDTO> staff = new HashSet<>(List.of(
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

        Mockito.verify(userServiceRequester, Mockito.times(1)).getUser(any());

        Assertions.assertTrue(userRepository.existsById(managerUUID));

        Assertions.assertNotNull(save);


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void saveWithFullStaffWorksFine() {


        UUID workerUUid1 = UUID.randomUUID();
        UUID workerUUid2 = UUID.randomUUID();
        UUID workerUUid3 = UUID.randomUUID();

        UserDTO worker1 = new UserDTO(workerUUid1);
        UserDTO worker2 = new UserDTO(workerUUid2);
        UserDTO worker3 = new UserDTO(workerUUid3);

        Set <UserDTO> staff = new HashSet<>();
        staff.add(worker1); staff.add(worker2);
        staff.add(worker3);


        UUID managerUUID = UUID.randomUUID();
        UserDTO manager = new UserDTO(managerUUID);
        manager.setRole(UserRole.MANAGER);

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("some_name");
        projectCreateDTO.setManager(manager);
        projectCreateDTO.setStaff(staff);

        doReturn(manager).when(userServiceRequester).getUser(managerUUID);
        doReturn(staff).when(userServiceRequester).getSetOfUserDTO(any());

        Project save = projectService.save(projectCreateDTO);

        Mockito.verify(userServiceRequester, Mockito.times(1)).getUser(any());

        Assertions.assertTrue(userRepository.existsById(managerUUID));

        Assertions.assertNotNull(save);

        Assertions.assertEquals(3, save.getStaff().size());
        Assertions.assertNotNull(save.getManager());


    }



    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateWorksFine() {

        UUID managerUUID = UUID.randomUUID();
        UserDTO newManager = new UserDTO(managerUUID);
        newManager.setRole(UserRole.MANAGER);

        doReturn(newManager).when(userServiceRequester).getUser(managerUUID);

        Project project = getInitProject();

        User initManager = project.getManager();

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO(
                "updated project", "updated description", newManager, null, ProjectStatus.ARCHIVED
        );

        Project newProject = projectService.update(project.getUuid(), project.getDtUpdate(), projectCreateDTO);

        Assertions.assertNotEquals(initManager.getUuid(), newProject.getManager().getUuid());

        Assertions.assertNotEquals(project.getName(), newProject.getName());

        Assertions.assertNotEquals(project.getDescription(), newProject.getDescription());

        Assertions.assertNotEquals(project.getStatus(), newProject.getStatus());

        Assertions.assertNull(newProject.getStaff());

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateThrowsOnUnknownUuidOrVersion() {

        UUID managerUUID = UUID.randomUUID();
        UserDTO newManager = new UserDTO(managerUUID);
        newManager.setRole(UserRole.USER);

        Project project = getInitProject();

        Set<User> staff = project.getStaff();

        doReturn(newManager).when(userServiceRequester).getUser(managerUUID);

        Set<UserDTO> changedStaff = staff.stream().limit(2).map(x -> new UserDTO(x.getUuid())).collect(Collectors.toSet());

        UserDTO nonExistingStaffMember = new UserDTO(UUID.randomUUID());

        changedStaff.add(nonExistingStaffMember);

        doReturn(new HashSet<>()).when(userServiceRequester).getSetOfUserDTO(any());

        newManager.setRole(UserRole.ADMIN);


        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO(
                "updated project", "updated description", newManager, changedStaff, ProjectStatus.ARCHIVED
        );


        StructuredException structuredException = Assertions.assertThrows(StructuredException.class, () ->
                projectService.update(project.getUuid(), project.getDtUpdate(), projectCreateDTO));

        Assertions.assertEquals(2,structuredException.getSize());

    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateThrowsOnUnknownManagerAndOneFromStaff() {


        Project project = getInitProject();

        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO(
                "updated project", "updated description", null, null, ProjectStatus.ARCHIVED
        );

        Assertions.assertThrows(StructuredException.class, ()->
                projectService.update(UUID.randomUUID(), project.getDtUpdate(), projectCreateDTO));

        Assertions.assertThrows(StructuredException.class, ()->
                projectService.update(project.getUuid(), project.getDtUpdate().minusSeconds(1), projectCreateDTO));

    }


    @Test
    public void findForUserInContextAsManagerWorks()  {

        Project probe = getInitProject();

        User manager = probe.getManager();

        doReturn(manager).when(userService).findUserInCurrentContext();

        Project target = projectService.findByUUIDAndUserInContext(probe.getUuid());

        Assertions.assertNotNull(target);

        Assertions.assertEquals(manager.getUuid(),target.getManager().getUuid());

    }

    @Test
    public void findForUserInContextAsStaffWorks()  {

        Project probe = getInitProject();

        Iterator<User> iterator = probe.getStaff().iterator();

        User inStaff = iterator.next();

        doReturn(inStaff).when(userService).findUserInCurrentContext();

        Project target = projectService.findByUUIDAndUserInContext(probe.getUuid());

        Assertions.assertNotNull(target);

        Assertions.assertTrue(
                ()-> target.getStaff().stream().anyMatch(x -> x.getUuid().equals(inStaff.getUuid()))
        );

    }

    @Test
    public void findForUserInContextNotInProjectThrows()  {

        Project probe = getInitProject();

        User inContext = userRepository.save(new User(UUID.randomUUID()));

        doReturn(inContext).when(userService).findUserInCurrentContext();

        Project notShown = projectService.findByUUIDAndUserInContext(probe.getUuid());

        Assertions.assertNull(notShown);

    }


    @Test
    public void findForUserNotInContextNotInProjectThrows() {

        Project probe = getInitProject();

        Assertions.assertThrows(AuthenticationFailedException.class, ()->
                projectService.findByUUIDAndUserInContext(probe.getUuid()));

    }

    @Test
    public void findForUserNotWorksIsEmptyList() {

        User notWorks = new User(UUID.randomUUID());

        List<Project> projectsWhereUserIsIn = projectService.getProjectsWhereUserIsInProject(notWorks);

        Assertions.assertEquals(0, projectsWhereUserIsIn.size());

    }

    @Test
    public void findForUserNotWorksIs3ElementsList() {

        User works = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        List<Project> projectsWhereUserIsIn = projectService.getProjectsWhereUserIsInProject(works);

        Assertions.assertEquals(3, projectsWhereUserIsIn.size());

    }


    @Test
    public void findPageUserInContextDoesNotWorkIsZero()  {

        doReturn(new User(UUID.randomUUID())).when(userService).findUserInCurrentContext();

        Page<Project> pageForUserInContextAndShowArchived = projectService.getPageForUserInContextAndInProjectAndShowArchived(0, 20, true);

        long totalElements = pageForUserInContextAndShowArchived.getTotalElements();

        Assertions.assertEquals(0, totalElements);

    }

    @Test
    public void findPageUserInContextWorksReturnsShowArchived()  {

        User worksIn3Projects = userRepository.findById(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS).orElseThrow();

        doReturn(worksIn3Projects).when(userService).findUserInCurrentContext();

        Page<Project> pageForUserInContextAndShowArchived = projectService.getPageForUserInContextAndInProjectAndShowArchived(0, 20, true);

        long totalElements = pageForUserInContextAndShowArchived.getTotalElements();

        Assertions.assertEquals(3, totalElements);

    }

    @Test
    public void findPageUserInContextWorksReturnsNotShowArchived()  {

        User worksIn3Projects = userRepository.findById(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS).orElseThrow();

        doReturn(worksIn3Projects).when(userService).findUserInCurrentContext();

        Page<Project> pageForUserInContextAndShowArchived = projectService.getPageForUserInContextAndInProjectAndShowArchived(0, 20, false);

        long totalElements = pageForUserInContextAndShowArchived.getTotalElements();

        Assertions.assertEquals(2, totalElements);

    }

    @Test
    public void findProjectByUuidAndUserManager()  {

        User worksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        boolean exists = projectService.userIsInProject(worksIn3Projects, INIT_PROJECT_UUID);

        Assertions.assertTrue(exists);


    }

    @Test
    public void findProjectByUuidAndUserStaff()  {

        User worksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        boolean exists = projectService.userIsInProject(worksIn3Projects, PROJECT2_UUID);

        Assertions.assertTrue(exists);

    }


    @Test
    public void findProjectByUuidAndUserIsNotIn()  {

        User worksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        boolean exists = projectService.userIsInProject(worksIn3Projects, PROJECT4_UUID);

        Assertions.assertFalse(exists);

    }

    @Test
    public void findProjectsWhereUserIsInProjectByUuidFilter()  {

        User worksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        List<UUID> uuidList = List.of(INIT_PROJECT_UUID, PROJECT2_UUID);

        List<Project> projectsWhereUserIsInProject = projectService.getProjectsWhereUserIsInProject(worksIn3Projects, uuidList);

        Assertions.assertEquals(2, projectsWhereUserIsInProject.size());


    }


    @Test
    public void findProjectsWhereUserIsInProjectByUuidFilterWith1NotApplicable()  {

        User worksIn3Projects = new User(USER_UUID_IS_MANAGER_AND_STAFF_IN_3_PROJECTS);

        List<UUID> uuidList = List.of(INIT_PROJECT_UUID, PROJECT2_UUID, PROJECT4_UUID);

        List<Project> projectsWhereUserIsInProject = projectService.getProjectsWhereUserIsInProject(worksIn3Projects, uuidList);

        Assertions.assertEquals(2, projectsWhereUserIsInProject.size());


    }


    @Test
    public void findPageUserNotInContextNotInProjectThrows() {

        Assertions.assertThrows(AuthenticationFailedException.class, ()->
                projectService.getPageForUserInContextAndInProjectAndShowArchived(0,2, true));
    }

    private Project getInitProject() {
        Project probe = new Project();
        probe.setName("Init project");
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues();

        Project project = projectRepository.findOne(Example.of(probe, matcher))
                .orElseThrow(RuntimeException::new);
        return project;
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


        Project project = new Project(INIT_PROJECT_UUID);

        project.setManager(userWorksIn3Projects);
        project.setStaff(Set.of(users.get(0), users.get(1), users.get(2)));
        project.setStatus(ProjectStatus.ACTIVE);
        project.setName("Init project");
        project.setDescription("Init project with 3 staff");

        projectRepository.saveAndFlush(project);

        Project project1 = new Project(PROJECT2_UUID);

        project1.setManager(users.get(5));
        project1.setStaff(Set.of(users.get(6), users.get(7), userWorksIn3Projects));
        project1.setStatus(ProjectStatus.ARCHIVED);
        project1.setName("Init project2");
        project1.setDescription("Init project2 with 3 staff archived");

        projectRepository.saveAndFlush(project1);


        Project project2 = new Project(PROJECT3_UUID);

        project2.setManager(userWorksIn3Projects);
        project2.setStatus(ProjectStatus.ACTIVE);
        project2.setName("Init project3");
        project2.setDescription("Init project3 with 0 staff");

        projectRepository.saveAndFlush(project2);

        Project project3 = new Project(PROJECT4_UUID);

        project3.setManager(users.get(0));
        project1.setStaff(Set.of(users.get(6), users.get(7), users.get(2)));
        project3.setStatus(ProjectStatus.ACTIVE);
        project3.setName("Init project4");
        project3.setDescription("Init project4 with 3 staff");

        projectRepository.saveAndFlush(project3);


    }

}
