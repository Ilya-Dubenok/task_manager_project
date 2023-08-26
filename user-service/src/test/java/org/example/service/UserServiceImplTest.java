package org.example.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolationException;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.example.service.utils.AuditMessagesFormer;
import org.example.service.utils.ChangedFieldsOfEntitySearcher;
import org.example.service.utils.JsonAuditMessagesFormer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @SpyBean
    private IUserService userService;

    @Autowired
    private IUserRepository repository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    private ConversionService conversionService;

    @MockBean
    private ISenderInfoService senderInfoService;

    @Autowired
    private ChangedFieldsOfEntitySearcher<User> searcher;

    @Autowired
    private AuditMessagesFormer auditMessagesFormer;

    @Autowired
    private JsonAuditMessagesFormer jsonAuditMessagesFormer;

    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired @Qualifier("testWithoutSecurityContext")
    IUserService userService) {
        clearAndInitSchema(dataSource);
        fillUserTableWithDefaultValues(userService);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        doReturn(null).when(userService).getUserFromCurrentSecurityContext();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, userService);
        }

    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void userRoleAndSatusNullsAreNotPassed() {
        Assertions.assertThrows(
                ConstraintViolationException.class, () -> userService.save(
                        new UserCreateDTO("", "", UserRole.USER, null, "ps")
                )
        );
        Assertions.assertThrows(
                ConstraintViolationException.class, () -> userService.save(
                        new UserCreateDTO("", "", null, UserStatus.ACTIVATED, "ps")
                )
        );

        Assertions.assertThrows(
                ConstraintViolationException.class, () -> userService.save(
                        new UserCreateDTO("", "", null, null, "ps")
                )
        );

    }


    @Test
    public void finByEmailAndStatusIsActive() {

        User byEmailAndStatusIsActivated = repository.findByMailAndStatusEquals("a@gmail.com", UserStatus.ACTIVATED);

        Assertions.assertNotNull(byEmailAndStatusIsActivated);

    }

    @Test
    public void exceptionOnRoleAndStatusIsHandled() {


        ConstraintViolationException exception = Assertions.assertThrows(
                ConstraintViolationException.class,
                () -> userService.save(
                        new UserCreateDTO("", "", null, null, "ps"))
        );

        Assertions.assertEquals(5, exception.getConstraintViolations().size());


    }

    @Test
    public void findAll() {

        List<User> all = repository.findAll();

        List<UUID> uuidList = all.stream()
                .map(User::getUuid)
                .toList();

        List<User> users = userService.getList(uuidList);

        Assertions.assertEquals(
                all.size(), users.size()
        );

        long notMatchingUsersCount = all.stream().filter(
                x -> !users.contains(x)
        ).count();

        Assertions.assertEquals(0, notMatchingUsersCount);


    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateUser() {
        User user = repository.findAll().get(0);
        UserDTO userDTO = conversionService.convert(user, UserDTO.class);

        UUID uuid = userDTO.getUuid();
        Long dt_update_in_long = userDTO.getDtUpdate();

        LocalDateTime dt_update = conversionService.convert(dt_update_in_long, LocalDateTime.class);

        String oldPassword = user.getPassword();

        UserCreateDTO userCreateDTO = new UserCreateDTO(
                "new_mail@gmail.com", "new fio", UserRole.ADMIN, UserStatus.DEACTIVATED, "new password"
        );

        doReturn(null).when(userService).getUserFromCurrentSecurityContext();

        userService.update(uuid, dt_update, userCreateDTO);

        List<User> resultList;

        try (EntityManager entityManager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(criteriaBuilder.equal(root.get("mail"), "new_mail@gmail.com"));
            entityManager.getTransaction().begin();
            resultList = entityManager.createQuery(query).getResultList();
            entityManager.getTransaction().commit();
        }

        Assertions.assertEquals(1, resultList.size());
        User updatedUser = resultList.get(0);
        Assertions.assertEquals("new fio", updatedUser.getFio());
        Assertions.assertEquals(UserRole.ADMIN, updatedUser.getRole());
        Assertions.assertEquals(UserStatus.DEACTIVATED, updatedUser.getStatus());
        Assertions.assertNotEquals(oldPassword, updatedUser.getPassword());
        Assertions.assertNotEquals(dt_update, updatedUser.getDtUpdate());

    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void updateUserWithExistingEmail() {
        doReturn(null).when(userService).getUserFromCurrentSecurityContext();

        User user;
        try (EntityManager entityManager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(criteriaBuilder.equal(root.get("mail"), "a@gmail.com"));
            entityManager.getTransaction().begin();
            user = entityManager.createQuery(query).getResultList().get(0);
            entityManager.getTransaction().commit();
        }

        UserCreateDTO userCreateDTO = new UserCreateDTO(
                "aa@gmail.com", "fio", user.getRole(), user.getStatus(), user.getPassword()
        );


        UUID uuid = user.getUuid();

        Assertions.assertThrows(StructuredException.class,
                () -> userService.update(uuid, user.getDtUpdate(), userCreateDTO));

    }

    @Test
    public void jsonUpdateInfoWorks() {

        User user1 = new User(UUID.randomUUID(),"old_mal","old_fio",UserRole.USER, UserStatus.ACTIVATED,"12345");
        User user2 = new User(user1.getUuid(), "old_mal", "new_fio", UserRole.ADMIN, UserStatus.ACTIVATED, "123456");

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formUserUpdateAuditMessage(user1, user2));
        System.out.println(s);

    }

    @Test
    public void jsonCreateInfoWorks() {
        User user1 = new User(UUID.randomUUID(),"old_mal","old_fio",UserRole.USER, UserStatus.ACTIVATED,"12345");

        String s = Assertions.assertDoesNotThrow(() -> jsonAuditMessagesFormer.formUserCreatedAuditMessage(user1));
        System.out.println(s);

    }



    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void setUserActiveByEmail() {

        String mail = "test@mail.ru";
        UserCreateDTO userCreateDTO = new UserCreateDTO(
                mail, "test", UserRole.USER, UserStatus.WAITING_ACTIVATION, "test1"
        );

        doReturn(null).when(userService).getUserFromCurrentSecurityContext();

        userService.save(userCreateDTO);

        int i = userService.setUserActiveByEmail(mail);
        Assertions.assertEquals(1, i);

        User user;
        try (EntityManager entityManager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager()) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(criteriaBuilder.equal(root.get("mail"), mail));
            entityManager.getTransaction().begin();
            user = entityManager.createQuery(query).getResultList().get(0);
            entityManager.getTransaction().commit();
        }

        Assertions.assertEquals(
                UserStatus.ACTIVATED, user.getStatus()
        );


    }

    private static void clearAndInitSchema(DataSource dataSource) {

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }


    private static void fillUserTableWithDefaultValues(IUserService userService) {
        Stream.of(
                new UserCreateDTO(
                        "a@gmail.com", "one", UserRole.USER, UserStatus.ACTIVATED, "12345"
                ),
                new UserCreateDTO(
                        "aa@gmail.com", "two", UserRole.USER, UserStatus.ACTIVATED, "12345"
                ),
                new UserCreateDTO(
                        "aaa@gmail.com", "three", UserRole.USER, UserStatus.ACTIVATED, "12345"
                ),
                new UserCreateDTO(
                        "ab@gmail.com", "four", UserRole.USER, UserStatus.DEACTIVATED, "12345"
                ),
                new UserCreateDTO(
                        "abb@gmail.com", "five", UserRole.USER, UserStatus.WAITING_ACTIVATION, "12345"
                )
        ).forEach(userService::save);

    }
}
