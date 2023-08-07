package org.example.core.dto.project;

import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.ProjectStatus;

import java.util.Set;

public class ProjectCreateDTO {

    private String name;

    private String description;

    private UserDTO manager;

    private Set<UserDTO> staff;

    private ProjectStatus status;

    public ProjectCreateDTO() {
    }

    public ProjectCreateDTO(String name, String description, UserDTO manager, Set<UserDTO> staff, ProjectStatus status) {
        this.name = name;
        this.description = description;
        this.manager = manager;
        this.staff = staff;
        this.status = status;
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
