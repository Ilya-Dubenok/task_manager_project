package org.example.endpoint.web;


import com.jayway.jsonpath.JsonPath;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserLoginDTO;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServletTest {

    private static final String RESTORE_BASE_VALUES_AFTER_TAG = "modifying";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Autowired
    ConversionService conversionService;

    @Autowired
    private MappingJackson2HttpMessageConverter converter;

    @Autowired
    private PasswordEncoder encoder;


    @SpyBean
    private IUserRepository userRepository;

    @SpyBean
    private IUserService userService;

    @MockBean
    private ISenderInfoService senderInfoService;


    @Test
    public void getPagesOfUsersWithDefaultValues() throws Exception {


        ResultActions resultActions = this.mockMvc.perform(getMockRequestWithAdminAuthorization(get("/api/v1/users"))).andExpect(
                status().isOk()
        );

        resultActions.andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total_pages").value(5))
                .andExpect(jsonPath("$.total_elements").value(100))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.number_of_elements").value(20))
                .andExpect(jsonPath("$.last").value(false));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        JSONObject object = new JSONObject(contentAsString);

        JSONArray content = object.getJSONArray("content");

        Assertions.assertEquals(20, content.length());


    }


    @Test
    public void getPagesOfUsersWithProvidedPage() throws Exception {

        ResultActions resultActions = this.mockMvc.perform(
                getMockRequestWithAdminAuthorization(get("/api/v1/users?page=1"))
                )
                .andExpect(
                        status().isOk()
                );

        resultActions.andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total_pages").value(5))
                .andExpect(jsonPath("$.total_elements").value(100))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.number_of_elements").value(20))
                .andExpect(jsonPath("$.last").value(false));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        JSONObject object = new JSONObject(contentAsString);

        JSONArray content = object.getJSONArray("content");

        Assertions.assertEquals(20, content.length());


    }


    @Test
    public void getPagesOfUsersWithProvidedPageAndSize() throws Exception {

        ResultActions resultActions = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=6&size=15"))
                )
                .andExpect(
                        status().isOk()
                );

        resultActions.andExpect(jsonPath("$.number").value(6))
                .andExpect(jsonPath("$.size").value(15))
                .andExpect(jsonPath("$.total_pages").value(7))
                .andExpect(jsonPath("$.total_elements").value(100))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.number_of_elements").value(10))
                .andExpect(jsonPath("$.last").value(true));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        JSONObject object = new JSONObject(contentAsString);

        JSONArray content = object.getJSONArray("content");

        Assertions.assertEquals(10, content.length());

    }


    @Test
    public void getPagesOfUsersWithUnparsablePage() throws Exception {

        ResultActions resultActions = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=blablabla&size=15"))
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(
                        status().is(400)
                );

        resultActions.andExpect(jsonPath("$.logref").value("structured_error"));
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        JSONObject object = new JSONObject(contentAsString);

        JSONArray errors = object.getJSONArray("errors");

        Assertions.assertEquals(1, errors.length());

        String string = errors.get(0).toString();

        Map<String, String> map = JsonPath.read(string, "$");

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("page", map.get("field"));
        Assertions.assertTrue(map.get("message").startsWith("page не может быть"));


    }


    @Test
    public void getPagesOfUsersWithUnparsableSize() throws Exception {

        ResultActions resultActions = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=0&size=blablabla"))
                                .characterEncoding("UTF-8")
                )
                .andDo(print())
                .andExpect(
                        status().is(400)
                );

        resultActions.andExpect(jsonPath("$.logref").value("structured_error"));
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        JSONObject object = new JSONObject(contentAsString);

        JSONArray errors = object.getJSONArray("errors");

        Assertions.assertEquals(1, errors.length());

        String string = errors.get(0).toString();

        Map<String, String> map = JsonPath.read(string, "$");

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals("size", map.get("field"));
        Assertions.assertTrue(map.get("message").startsWith("size не может быть"));


    }

    @Test
    public void getPageOfUsersIsDetermined() throws Exception {

        String firstRequest = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=3&size=15"))
                )
                .andExpect(
                        status().isOk()
                ).andReturn().getResponse().getContentAsString();

        String secondRequest = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=3&size=15"))
                )
                .andExpect(
                        status().isOk()
                ).andReturn().getResponse().getContentAsString();


        JSONArray array1 = new JSONObject(firstRequest).getJSONArray("content");
        JSONArray array2 = new JSONObject(secondRequest).getJSONArray("content");


        List<String> list = fillListOfStringFromJSONArrayValues(array1);
        List<String> list2 = fillListOfStringFromJSONArrayValues(array2);

        Assertions.assertEquals(
                list, list2
        );


    }


    @Test
    public void getPageOfUsersDoesNotOverlap() throws Exception {

        String firstRequest = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=3&size=15"))
                )
                .andExpect(
                        status().isOk()
                ).andReturn().getResponse().getContentAsString();

        String secondRequest = this.mockMvc.perform(
                        getMockRequestWithAdminAuthorization(get("/api/v1/users?page=4&size=15"))
                )
                .andExpect(
                        status().isOk()
                ).andReturn().getResponse().getContentAsString();

        JSONArray array1 = new JSONObject(firstRequest).getJSONArray("content");
        JSONArray array2 = new JSONObject(secondRequest).getJSONArray("content");


        List<String> list = fillListOfStringFromJSONArrayValues(array1);
        List<String> list2 = fillListOfStringFromJSONArrayValues(array2);


        boolean b = list.stream().anyMatch(
                x -> list2.stream().anyMatch(
                        y -> y.equals(x)
                )
        );

        Assertions.assertFalse(b);


    }


    @Test
    public void getPageOfUsersDoesNotOverlapAtAll() throws Exception {


        Stream<String> responceEntityStream = Stream.iterate(0, x -> x + 1)
                .limit(7)
                .map(x -> "/api/v1/users?page=" + x + "&size=15")
                .map(
                        x -> {
                            try {
                                return this.mockMvc.perform(
                                                getMockRequestWithAdminAuthorization(get(x))
                                        )
                                        .andExpect(
                                                status().isOk()
                                        ).andReturn().getResponse().getContentAsString();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                );

        Set<String> finalContent = responceEntityStream
                .map(x -> {
                            try {
                                return new JSONObject(x).getJSONArray("content");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .map(jsonArray -> {
                    try {
                        return fillListOfStringFromJSONArrayValues(jsonArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(
                        List::stream
                )
                .collect(Collectors.toSet());

        Assertions.assertEquals(100, finalContent.size());


    }

    @Test
    public void putTest() throws Exception {

        String string = converter.getObjectMapper().writeValueAsString(
                new UserCreateDTO("dubenokilya@gmail.com", "fio", UserRole.USER, UserStatus.WAITING_ACTIVATION,
                        "12345")
        );

        this.mockMvc.perform(
                getMockRequestWithAdminAuthorization(put("/api/v1/users/".concat(UUID.randomUUID().toString()) + "/dt_update/" + 659563531L))
                        .contentType("application/json")
                        .content(string)

        ).andExpect(status().isBadRequest());

    }


    @BeforeAll
    public static void initWithDefaultValues(@Autowired DataSource dataSource, @Autowired IUserRepository userRepository, @Autowired PasswordEncoder passwordEncoder) {
        clearAndInitSchema(dataSource);
        fillWithDefaultValues(userRepository, passwordEncoder);
    }

    @AfterEach
    public void checkForClearance(TestInfo info) {
        Set<String> tags = info.getTags();
        if (tags.contains(RESTORE_BASE_VALUES_AFTER_TAG)) {
            initWithDefaultValues(dataSource, userRepository, encoder);
            fillWithDefaultValues(userRepository, encoder);
        }

    }



    private  MockHttpServletRequestBuilder getMockRequestWithAdminAuthorization(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        return mockHttpServletRequestBuilder.header(
                "Authorization", "Bearer " + getAdminToken()
        );
    }

    private  MockHttpServletRequestBuilder getMockRequestWithUserAuthorization(MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        return mockHttpServletRequestBuilder.header(
                "Authorization", "Bearer " + getUserToken()
        );
    }

    private String getAdminToken() throws Exception {
        String postAdminLogin = converter.getObjectMapper().writeValueAsString(
                new UserLoginDTO(
                        "admin@gmail.com",
                        "admin123"
                )
        );

        ResultActions perform = this.mockMvc.perform(post("/api/v1/users/login/token")
                .contentType("application/json").content(
                        postAdminLogin
                ));

        return perform.andReturn().getResponse().getHeader("Bearer ");
    }

    private String getUserToken() throws Exception {
        String postAdminLogin = converter.getObjectMapper().writeValueAsString(
                new UserLoginDTO(
                        "dummy1@gmail.com",
                        "password"
                )
        );

        ResultActions perform = this.mockMvc.perform(post("/api/v1/users/login/token")
                .contentType("application/json").content(
                        postAdminLogin
                ));

        return perform.andReturn().getResponse().getHeader("Bearer ");
    }


    private static void clearAndInitSchema(DataSource dataSource) {

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("sql/drop_schema.sql"),
                new ClassPathResource("sql/create_schema_and_tables.sql")
        );
        databasePopulator.execute(dataSource);
    }




    private static void fillWithDefaultValues(IUserRepository userRepository, PasswordEncoder encoder) {
        Stream.iterate(1, x -> x + 1).limit(99)
                .map(
                        x -> new User(
                                UUID.randomUUID(),
                                "dummy".concat(x.toString()).concat("@gmail.com"),
                                "dummyFio",
                                UserRole.USER,
                                UserStatus.ACTIVATED,
                                encoder.encode("password")
                        )
                )
                .forEach(userRepository::save);

        userRepository.save(
                new User(
                        UUID.randomUUID(),
                        "admin@gmail.com",
                        "admin",
                        UserRole.ADMIN,
                        UserStatus.ACTIVATED,
                        encoder.encode("admin123")
                )
        );

    }

    private static List<String> fillListOfStringFromJSONArrayValues(JSONArray array) throws JSONException {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.get(i).toString());

        }

        return list;
    }

}
