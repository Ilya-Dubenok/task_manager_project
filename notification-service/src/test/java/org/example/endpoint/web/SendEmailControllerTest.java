package org.example.endpoint.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.core.dto.SimpleEmailTemplateDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SendEmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MappingJackson2HttpMessageConverter springMvcJacksonConverter;


    @Test
    public void converstionIsOk() throws Exception {

        String mail = "dubenokilya@gmail.com";
        String subject = "SUBJECT";
        String text = "TEXT";
        URI replyTo = URI.create(
                "http://localhost");
        SimpleEmailTemplateDTO dto = new SimpleEmailTemplateDTO(
                mail, subject, text, replyTo
        );

        ObjectMapper objectMapper = springMvcJacksonConverter.getObjectMapper();
        String dtoInString = objectMapper.writeValueAsString(dto);

        String contentAsString = mockMvc.perform(post("/email")
                        .contentType("application/json")
                        .content(dtoInString))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getRequest().getContentAsString();

        SimpleEmailTemplateDTO dto1 = objectMapper.readValue(contentAsString, SimpleEmailTemplateDTO.class);

        Assertions.assertEquals(mail, dto1.getTo());
        Assertions.assertEquals(subject, dto1.getSubject());
        Assertions.assertEquals(text, dto1.getText());
        Assertions.assertEquals(replyTo, dto1.getReplyTo());

    }


    @Test
    public void nullReplyToIsRecognized() throws Exception {

        String mail = "dubenokilya@gmail.com";
        String subject = "SUBJECT";
        String text = "TEXT";
        SimpleEmailTemplateDTO dto = new SimpleEmailTemplateDTO(

        );

        dto.setTo(mail);
        dto.setSubject(subject);
        dto.setText(text);

        ObjectMapper objectMapper = springMvcJacksonConverter.getObjectMapper();
        String dtoInString = objectMapper.writeValueAsString(dto);

        String contentAsString = mockMvc.perform(post("/email")
                        .contentType("application/json")
                        .content(dtoInString))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getRequest().getContentAsString();

        SimpleEmailTemplateDTO dto1 = objectMapper.readValue(contentAsString, SimpleEmailTemplateDTO.class);

        Assertions.assertEquals(mail, dto1.getTo());
        Assertions.assertEquals(subject, dto1.getSubject());
        Assertions.assertEquals(text, dto1.getText());
        Assertions.assertEquals(null, dto1.getReplyTo());


    }

}
