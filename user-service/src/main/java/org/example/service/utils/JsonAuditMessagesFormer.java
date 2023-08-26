package org.example.service.utils;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.dao.entities.user.User;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JsonAuditMessagesFormer {

    private ChangedFieldsOfEntitySearcher<User> userChangedFieldsOfEntitySearcher;

    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    private final String USER_CREATED_MESSAGE = "Пользователь был создан";

    private final String USER_REGISTERED_MESSAGE = "Пользователь был зарегистрирован";


    public JsonAuditMessagesFormer(ChangedFieldsOfEntitySearcher<User> userChangedFieldsOfEntitySearcher, MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.userChangedFieldsOfEntitySearcher = userChangedFieldsOfEntitySearcher;
        this.mappingJackson2HttpMessageConverter = mappingJackson2HttpMessageConverter;
    }

    public String formUserCreatedAuditMessage(User user) {

        ChangeDTOInfo changeDTOInfo = new ChangeDTOInfo();

        changeDTOInfo.setType(Type.CREATE);

        Map<String, Object> fieldsAndValues = userChangedFieldsOfEntitySearcher.getFieldsAndValues(user);

        fieldsAndValues.forEach(
                (k,v)->{
                    if (null == v) {
                        fieldsAndValues.put(k,"not_to_disclose");
                    }
                }
        );

        changeDTOInfo.setContent(fieldsAndValues);

        try {

            return mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(changeDTOInfo);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public String formUserRegisteredAuditMessage(User user) {

        ChangeDTOInfo changeDTOInfo = new ChangeDTOInfo();

        RegisterDTO registerDTO = new RegisterDTO();

        registerDTO.setMail(user.getMail());

        changeDTOInfo.setType(Type.REGISTER);

        changeDTOInfo.setContent(registerDTO);

        try{
            return mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(changeDTOInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public String formUserUpdateAuditMessage(User user1, User user2) {
        Map<String, Pair<String, String>> changes = userChangedFieldsOfEntitySearcher.getChanges(
                user1, user2
        );
        return parseUpdatesToAuditMessage(changes);
    }


    private String parseUpdatesToAuditMessage(Map<String, Pair<String, String>> updates) {

        List<UpdateDTO> updatesList = new ArrayList<>();

        updates.forEach((key, pair) -> {

            UpdateDTO updateDTO = new UpdateDTO();
            updateDTO.setField(key);

            if (pair == null) {

                updateDTO.setOldValue("not_to_disclose");

                updateDTO.setNewValue("not_to_disclose");

                updatesList.add(updateDTO);

                return;

            }

            updateDTO.setOldValue(pair.getKey());

            updateDTO.setNewValue(pair.getValue());

            updatesList.add(updateDTO);

        });

        ChangeDTOInfo changeDTOInfo = new ChangeDTOInfo();
        changeDTOInfo.setType(Type.UPDATE);
        changeDTOInfo.setContent(updatesList);

        try {

            return mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(changeDTOInfo);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public String getAuditRegisteredMessage() {
        return USER_REGISTERED_MESSAGE;
    }


    private class ChangeDTOInfo{

        private Type type;

        private Object content;

        public ChangeDTOInfo() {
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }
    }



    private class RegisterDTO {

        private String mail;

        public RegisterDTO() {
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }
    }

    private class UpdateDTO {

        private String field;

        private String oldValue;

        private String newValue;

        public UpdateDTO() {
        }

        public UpdateDTO(String field, String oldValue, String newValue) {
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }
    }

    private enum Type {

        @JsonProperty("create")
        CREATE,
        @JsonProperty("register")
        REGISTER,
        @JsonProperty("update")
        UPDATE

    }
}
