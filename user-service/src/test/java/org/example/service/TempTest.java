package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import org.example.core.dto.user.UserCreateDTO;
import org.example.service.api.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TempTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    @Autowired
    IUserService userService;



    @Test
    public void test3() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setFio("22"); dto.setMail("2323323");
        ObjectMapper objectMapper = springMvcJacksonConverter.getObjectMapper();
        String string = objectMapper.writeValueAsString(dto);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/users").content(
                string
        ).contentType("application/json")).andDo(print()).andExpect(
                status().isOk()
        );


    }

    @Test
    public void test4() {

        String myNewField = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "myNewField");
        System.out.println(myNewField);
    }



}
