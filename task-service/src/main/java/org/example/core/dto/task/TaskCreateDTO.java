package org.example.core.dto.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.validation.NotNullInternalUUID;
import org.example.dao.entities.project.ProjectStatus;

public class TaskCreateDTO {

    @NotNull(message = "нужно передать проект")
    @NotNullInternalUUID
    private ProjectUuidDTO project;

    @NotBlank(message = "не должен быть пустым")
    private String title;

    private String description;

    private ProjectStatus status;

    @NotNullInternalUUID
    private UserDTO implementer;

    public TaskCreateDTO() {
    }

    public TaskCreateDTO(ProjectUuidDTO project, String title, String description, ProjectStatus status, UserDTO implementer) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
        this.implementer = implementer;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public UserDTO getImplementer() {
        return implementer;
    }

    public void setImplementer(UserDTO implementer) {
        this.implementer = implementer;
    }
}

