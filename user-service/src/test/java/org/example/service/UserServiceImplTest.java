package org.example.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.example.utils.ChangedFieldOfEntitySearcher;
import org.junit.jupiter.api.*;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.reflections.ReflectionUtils.Fields;

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
    private ChangedFieldOfEntitySearcher<User> searcher;

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

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userService.update(uuid, user.getDtUpdate(), userCreateDTO));

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

    @Test
    public void testGeneric() {
        User user1 = new User(
                UUID.randomUUID(), "initial@mail.ru", "initial.fio", UserRole.USER, UserStatus.WAITING_ACTIVATION,
                "initial_pass"
        );

        User user2 = new User(
                UUID.randomUUID(), "new@mail.ru", "initial.fio", UserRole.ADMIN, UserStatus.ACTIVATED,
                "new_pass"
        );

        Set<Class<? extends Annotation>> notScannedAnnotationsClasses = new HashSet<>();

        //take it
        notScannedAnnotationsClasses.addAll(
                List.of(Id.class, CreatedDate.class, LastModifiedDate.class)
        );

        //and it
        Set<String> forbiddenNames = new HashSet<>();
        forbiddenNames.add("password");

        Set<Field> fields = ReflectionUtils.get(Fields.of(User.class),

                field -> {
                    Annotation[] annotations = field.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (
                                notScannedAnnotationsClasses.contains(annotation.annotationType())
                        )
                            return false;
                    }
                    return true;
                }
        );

        fields.forEach(
                field ->
                {
                    if (forbiddenNames.contains(field.getName())) {
                        System.out.println(field.getName());
                        return;
                    }

                    if (!field.canAccess(user1)) {

                        try {
                            field.setAccessible(true);

                            System.out.println(field.get(user1));


                        } catch (InaccessibleObjectException | IllegalAccessException e) {


                        } finally {
                            field.setAccessible(false);

                        }

                    } else {
                        try {
                            System.out.println(field.get(user1));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Test
    public void testBuilder() {
        ChangedFieldOfEntitySearcher<User> userAnalyzer = new ChangedFieldOfEntitySearcher
                .Builder<>(User.class)
                .setNotToScanAnnotations(List.of(
                        Id.class, CreatedDate.class
                ))
                .setFieldsWithNoValuesToDisclose(List.of("password"))
                .build();

        User user1 = new User(
                UUID.randomUUID(), "initial@mail.ru", "initial.fio", UserRole.USER, UserStatus.WAITING_ACTIVATION,
                "initial_pass"
        );

        User user2 = new User(
                UUID.randomUUID(), "new@mail.ru", "initial.fio", UserRole.ADMIN, UserStatus.ACTIVATED,
                "new_pass"
        );

        Map<String, Pair<String, String>> changes = userAnalyzer.getChanges(user1, user2);

        System.out.println(parseUpdatesToAuditMessage(changes));


    }

    private String parseUpdatesToAuditMessage(Map<String, Pair<String, String>> updates) {
        StringBuilder stringBuilder = new StringBuilder(
                "Запись была обновлена. Следующие изменения:"
        );

        updates.forEach((key, pair) -> {
            stringBuilder
                    .append(" ")
                    .append(key);
            if (pair == null) {
                stringBuilder.append("(не отображается).");
                return;
            }
            stringBuilder
                    .append(", старое значение->")
                    .append(pair.getKey())
                    .append(", новое значение->")
                    .append(pair.getValue())
                    .append(".");


        });

        return stringBuilder.toString();
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
