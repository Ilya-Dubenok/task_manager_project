package org.example.service;

import org.example.core.dto.UserRegistrationDTO;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IUserRepository;
import org.example.dao.api.IVerificationInfoRepository;
import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.IEmailService;
import org.example.service.api.IUserService;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationServiceImplTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "restore_base_value";


    @Autowired
    private IUserService userService;

    @SpyBean
    IUserRepository userRepository;

    @SpyBean
    IVerificationInfoRepository verificationInfoRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @MockBean
    IEmailService spyEmailService;

    @Autowired
    IAuthenticationService authenticationService;

    @Test
    @Tag(RESTORE_BASE_VALUES_AFTER_TAG)
    public void justPractice() {


        Assertions.assertDoesNotThrow(() -> authenticationService.registerUser(
                new UserRegistrationDTO("fake2", "fio", "123")
        ));

        InOrder inOrder = inOrder(verificationInfoRepository, spyEmailService);

        inOrder.verify(verificationInfoRepository).cleanOldCodes(any(), any());
        inOrder.verify(verificationInfoRepository).save(any(VerificationInfo.class));
        inOrder.verify(spyEmailService, times(1)).sendVerificationCodeMessage(anyString(), anyInt());


    }


    @Test
    public void exceptionsThrown() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(null, null, null);

        StructuredException exception3 = Assertions.assertThrows(
                StructuredException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        userRegistrationDTO.setMail("someMail");

        StructuredException exception2 = Assertions.assertThrows(
                StructuredException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );
        userRegistrationDTO.setFio("someFio");

        StructuredException exception1 = Assertions.assertThrows(
                StructuredException.class, () -> authenticationService.registerUser(
                        userRegistrationDTO
                )
        );

        Assertions.assertEquals(3, exception3.getSize());
        Assertions.assertEquals(2, exception2.getSize());
        Assertions.assertEquals(1, exception1.getSize());


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
                        UUID.randomUUID(), "mail", 3334, LocalDateTime.now().minusMinutes(20),
                        EmailStatus.WAITING_TO_BE_SENT, 1
                )
        );

        StructuredException exception = Assertions.assertThrows(
                StructuredException.class, () ->
                        authenticationService.verifyUserWithCode(
                                3334, "mail"
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
