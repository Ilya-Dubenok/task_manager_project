package org.example.core.dto.project;

import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.ProjectStatus;

import java.util.Set;
import java.util.UUID;

public class ProjectDTO {

    private UUID uuid;

    private Long dtCreate;

    private Long dtUpdate;

    private String name;

    private String description;

    private UserDTO manager;

    private Set<UserDTO> staff;

    private ProjectStatus status;

    public ProjectDTO() {
    }

    public ProjectDTO(UUID uuid, Long dtCreate, Long dtUpdate, String name, String description, UserDTO manager, Set<UserDTO> staff, ProjectStatus status) {
        this.uuid = uuid;
        this.dtCreate = dtCreate;
        this.dtUpdate = dtUpdate;
        this.name = name;
        this.description = description;
        this.manager = manager;
        this.staff = staff;
        this.status = status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserDTO getManager() {
        return manager;
    }

    public void setManager(UserDTO manager) {
        this.manager = manager;
    }

    public Set<UserDTO> getStaff() {
        return staff;
    }

    public void setStaff(Set<UserDTO> staff) {
        this.staff = staff;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }


}
