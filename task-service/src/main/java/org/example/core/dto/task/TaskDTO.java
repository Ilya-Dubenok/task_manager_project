package org.example.core.dto.task;

import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.task.TaskStatus;

import java.util.UUID;

public class TaskDTO {

    private UUID uuid;

    private Long dtCreate;

    private Long dtUpdate;

    private ProjectUuidDTO project;

    private String title;

    private String description;

    private TaskStatus status;

    private UserDTO implementer;

    public TaskDTO() {
    }

    public TaskDTO(UUID uuid, Long dtCreate, Long dtUpdate, ProjectUuidDTO project, String title, String description, TaskStatus status, UserDTO implementer) {
        this.uuid = uuid;
        this.dtCreate = dtCreate;
        this.dtUpdate = dtUpdate;
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
        this.implementer = implementer;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Long getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Long dtCreate) {
        this.dtCreate = dtCreate;
    }

    public Long getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Long dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public ProjectUuidDTO getProject() {
        return project;
    }

    public void setProject(ProjectUuidDTO project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public UserDTO getImplementer() {
        return implementer;
    }

    public void setImplementer(UserDTO implementer) {
        this.implementer = implementer;
    }
}

