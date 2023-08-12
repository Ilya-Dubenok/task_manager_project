package org.example.service;


import jakarta.validation.ConstraintViolationException;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.example.service.api.IUserServiceRequester;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @Autowired
    private DataSource dataSource;

    @SpyBean
    private IUserRepository userRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private ConversionService conversionService;

    @SpyBean
    private IUserServiceRequester userServiceRequester;

    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource,
                                             @Autowired IUserRepository userRepository) {
        clearAndInitSchema(dataSource);
        fillDataBaseWithDefaultValues(userRepository);
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
            initWithDefaultValues(dataSource, userRepository);
        }

    }


    @Test
    public void whenNothingFoundThrows() {
        Mockito.doReturn(null).when(userServiceRequester).getUser(any());

        Assertions.assertThrows(GeneralException.class,
                () -> userService.findAndSave(new UserDTO(UUID.randomUUID())));


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void whenFoundNotThrows() {
        long initial = userRepository.count();
        Mockito.doReturn(new UserDTO(UUID.randomUUID())).when(userServiceRequester).getUser(any());

        Assertions.assertDoesNotThrow(
                () -> userService.findAndSave(new UserDTO(UUID.randomUUID()))
        );

        long count = userRepository.count();
        Assertions.assertNotEquals(initial, count);

    }

    @Test
    public void whenRoleIsMissing() {
        UserDTO notManager = new UserDTO(UUID.randomUUID());
        notManager.setRole(UserRole.USER);

        Mockito.doReturn(notManager).when(userServiceRequester).getUser(any());

        Assertions.assertThrows(ConstraintViolationException.class,
                () -> userService.findByRoleAndSave(notManager, UserRole.MANAGER));


    }


    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void whenRoleIsNotMissing() {
        long initial = userRepository.count();
        UserDTO manager = new UserDTO(UUID.randomUUID());
        manager.setRole(UserRole.MANAGER);

        Mockito.doReturn(manager).when(userServiceRequester).getUser(any());

        Assertions.assertDoesNotThrow(
                () -> userService.findByRoleAndSave(manager, UserRole.MANAGER)
        );

        long count = userRepository.count();
        Assertions.assertNotEquals(initial, count);
    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void whenAllAreReturned() {

        List<UserDTO> storedInDataBase = userRepository.findAll().stream()
                .limit(5)
                .map(x -> new UserDTO(x.getUuid()))
                .toList();

        List<UserDTO> notStored = Stream.generate(
                () -> new UserDTO(UUID.randomUUID()))
                .limit(5)
                .toList();

        List<UserDTO> combined = new ArrayList<>(storedInDataBase);
        combined.addAll(notStored);
        Collections.shuffle(combined);


        Mockito.doReturn(new HashSet<>(notStored)).when(userServiceRequester).getSetOfUserDTOs(Mockito.anyList());

        Assertions.assertDoesNotThrow(()->userService.findAllAndSave(new HashSet<>(combined)));


    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void whenNotAllAreReturned() {

        List<UserDTO> storedInDataBase = userRepository.findAll().stream()
                .limit(5)
                .map(x -> new UserDTO(x.getUuid()))
                .toList();

        List<UserDTO> notStored = new ArrayList<>();
        Stream.generate(
                        () -> new UserDTO(UUID.randomUUID()))
                .limit(5)
                .forEach(notStored::add);

        List<UserDTO> combined = new ArrayList<>(storedInDataBase);
        combined.addAll(notStored);
        Collections.shuffle(combined);
        notStored.remove(0);

        Mockito.doReturn(new HashSet<>(notStored)).when(userServiceRequester).getSetOfUserDTOs(Mockito.anyList());

        Assertions.assertThrows(ConstraintViolationException.class, ()->userService.findAllAndSave(new HashSet<>(combined)));



    }


    private static void fillDataBaseWithDefaultValues(IUserRepository userRepository) {
        userRepository.saveAll(
                Stream.generate(
                        () -> new User(UUID.randomUUID())
                ).limit(10).toList()
        );
    }

}
