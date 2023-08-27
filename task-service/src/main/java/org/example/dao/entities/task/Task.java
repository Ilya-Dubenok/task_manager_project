package org.example.dao.entities.task;

import jakarta.persistence.*;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.user.User;
import org.example.service.utils.annotations.SpecifiedScan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    private UUID uuid;

    @CreatedDate
    @Column(name = "dt_create")
    private LocalDateTime dtCreate;

    @LastModifiedDate
    @Version
    @Column(name = "dt_update")
    private LocalDateTime dtUpdate;

    @ManyToOne
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "task_project_foreign_key"))
    @SpecifiedScan(fieldsToScan = {"uuid"})
    private Project project;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "task_user_foreign_key"))
    private User implementer;

    public Task() {
    }

    public Task(UUID uuid) {
        this.uuid = uuid;
    }

    public Task(UUID uuid, LocalDateTime dtCreate, LocalDateTime dtUpdate, Project project, String title, String description, TaskStatus status, User implementer) {
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
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

    public User getImplementer() {
        return implementer;
    }

    public void setImplementer(User implementer) {
        this.implementer = implementer;
    }
}
