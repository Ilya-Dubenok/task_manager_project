package org.example.dao.entities.project;

import jakarta.persistence.*;
import org.example.dao.entities.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project",
        uniqueConstraints = @UniqueConstraint(name = "project_name_unique_constraint", columnNames = {"name"})
)
@EntityListeners(AuditingEntityListener.class)
public class Project {


    @Id
    private UUID uuid;

    @CreatedDate
    @Column(name = "dt_create")
    private LocalDateTime dtCreate;

    @LastModifiedDate
    @Version
    @Column(name = "dt_update")
    private LocalDateTime dtUpdate;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "manager", foreignKey = @ForeignKey(name = "project_users_foreign_key"))
    private User manager;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "projects_users",
            joinColumns = @JoinColumn(name = "project_uuid"),
            foreignKey = @ForeignKey(name = "projects_users_project_foreign_key"),
            inverseJoinColumns = @JoinColumn(name = "users_uuid"),
            inverseForeignKey = @ForeignKey(name = "projects_users_users_foreign_key")
    )
    private Set<User> staff;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    public Project() {
    }

    public Project(UUID uuid) {
        this.uuid = uuid;
    }

    public Project(UUID uuid, LocalDateTime dtCreate, LocalDateTime dtUpdate, String name, String description, User manager, Set<User> staff, ProjectStatus status) {
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

    public LocalDateTime getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(LocalDateTime dtCreate) {
        this.dtCreate = dtCreate;
    }

    public LocalDateTime getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(LocalDateTime dtUpdate) {
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

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public Set<User> getStaff() {
        return staff;
    }

    public void setStaff(Set<User> staff) {
        this.staff = staff;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
