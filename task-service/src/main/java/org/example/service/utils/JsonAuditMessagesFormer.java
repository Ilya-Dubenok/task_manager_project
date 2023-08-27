package org.example.service.utils;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JsonAuditMessagesFormer {

    private ChangedFieldsOfEntitySearcher<Project> projectChangedFieldsOfEntitySearcher;

    private ChangedFieldsOfEntitySearcher<Task> taskChangedFieldsOfEntitySearcher;

    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;


    public JsonAuditMessagesFormer(ChangedFieldsOfEntitySearcher<Project> projectChangedFieldsOfEntitySearcher,
                                   ChangedFieldsOfEntitySearcher<Task> taskChangedFieldsOfEntitySearcher,
                                   MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.projectChangedFieldsOfEntitySearcher = projectChangedFieldsOfEntitySearcher;
        this.taskChangedFieldsOfEntitySearcher = taskChangedFieldsOfEntitySearcher;
        this.mappingJackson2HttpMessageConverter = mappingJackson2HttpMessageConverter;
    }

    public String formObjectCreatedAuditMessage(Project project) {

        ChangeDTOInfo changeDTOInfo = new ChangeDTOInfo();

        changeDTOInfo.setType(Type.CREATE);

        Map<String, Object> fieldsAndValues = projectChangedFieldsOfEntitySearcher.getFieldsAndValues(project);

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


    public String formObjectCreatedAuditMessage(Task task) {

        ChangeDTOInfo changeDTOInfo = new ChangeDTOInfo();

        changeDTOInfo.setType(Type.CREATE);

        Map<String, Object> fieldsAndValues = taskChangedFieldsOfEntitySearcher.getFieldsAndValues(task);

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

    public String formObjectUpdatedAuditMessage(Project project1, Project project2) {
        Map<String, Pair<Object, Object>> changes = projectChangedFieldsOfEntitySearcher.getChanges(
                project1, project2
        );
        return parseUpdatesToAuditMessage(changes);
    }

    public String formObjectUpdatedAuditMessage(Task task1, Task task2) {
        Map<String, Pair<Object, Object>> changes = taskChangedFieldsOfEntitySearcher.getChanges(
                task1, task2
        );
        return parseUpdatesToAuditMessage(changes);
    }


    private String parseUpdatesToAuditMessage(Map<String, Pair<Object, Object>> updates) {

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

        private Object oldValue;

        private Object newValue;

        public UpdateDTO() {
        }

        public UpdateDTO(String field, Object oldValue, Object newValue) {
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

        public Object getOldValue() {
            return oldValue;
        }

        public void setOldValue(Object oldValue) {
            this.oldValue = oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public void setNewValue(Object newValue) {
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
