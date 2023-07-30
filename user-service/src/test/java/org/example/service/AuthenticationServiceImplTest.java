package org.example.service;

import jakarta.validation.ConstraintViolationException;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IUserRepository;
import org.example.dao.api.IVerificationInfoRepository;
import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.*;

@SpringBootTest
@ActiveProfiles("test")
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class AuthenticationServiceImplTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @Autowired
    private IUserService userService;

    @SpyBean
    private IUserRepository userRepository;

    @SpyBean
    private IVerificationInfoRepository verificationInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @MockBean
    private ISenderInfoService senderInfoService;

    @Autowired
    private IAuthenticationService authenticationService;

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void justPractice() {


        Assertions.assertDoesNotThrow(() -> authenticationService.registerUser(
                new UserRegistrationDTO("fake2@mail.com", "fio", "12334")
        ));

        InOrder inOrder = inOrder(verificationInfoRepository);

        inOrder.verify(verificationInfoRepository).cleanOldCodes(any(), any());
        inOrder.verify(verificationInfoRepository).save(any(VerificationInfo.class));


    }



    @Test
    public void exceptionsThrown() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(null, null, null);

        ConstraintViolationException exception3 = Assertions.assertThrows(
                ConstraintViolationException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        userRegistrationDTO.setMail("someMail@mail.com");

        ConstraintViolationException exception2 = Assertions.assertThrows(
                ConstraintViolationException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );
        userRegistrationDTO.setFio("someFio");

        ConstraintViolationException exception1 = Assertions.assertThrows(
                ConstraintViolationException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        Assertions.assertEquals(3, exception3.getConstraintViolations().size());
        Assertions.assertEquals(2, exception2.getConstraintViolations().size());
        Assertions.assertEquals(1, exception1.getConstraintViolations().size());

        userRegistrationDTO.setPassword("123456");

        Assertions.assertDoesNotThrow(() -> {
            authenticationService.registerUser(userRegistrationDTO);
        });


    }

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void noDuplicatesOfEmailInDatabase() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(
                "first@gmail.com", "fio", "password"
        );

        Assertions.assertDoesNotThrow(
                () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        userRegistrationDTO.setFio("anotherFio");
        userRegistrationDTO.setPassword("anotherPassword");


        StructuredException exception = Assertions.assertThrows(
                StructuredException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        Map<String, String> exceptionsMap = exception.getExceptionsMap();

        Assertions.assertEquals(1, exceptionsMap.size());
        Assertions.assertTrue(exceptionsMap.containsKey("mail"));


    }

    @Test
    public void oldCodesAreErased() {
        verificationInfoRepository.save(
                new VerificationInfo(
                        UUID.randomUUID(), "mail@mail.com", 3334, LocalDateTime.now().minusMinutes(20),
                        EmailStatus.WAITING_TO_BE_SENT, 1
                )
        );

        StructuredException exception = Assertions.assertThrows(
                StructuredException.class, () ->
                        authenticationService.verifyUserWithCode(
                                3334, "mail@mail.com"
                        )
        );

        Assertions.assertEquals(1, exception.getSize());
        Assertions.assertTrue(exception.getExceptionsMap().containsKey("mail"));

    }


    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource) {
        clearAndInitSchema(dataSource);
        fillWithDefaultValues();
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource);
            fillWithDefaultValues();
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


    private static void fillWithDefaultValues() {


    }


}
