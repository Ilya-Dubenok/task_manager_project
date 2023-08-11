package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.exception.AuthenticationFailedException;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.ITaskRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.ITaskService;
import org.example.service.api.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Validated
@Service
public class TaskServiceImpl implements ITaskService {

    private IProjectService projectService;

    private IUserService userService;

    private ITaskRepository taskRepository;


    public TaskServiceImpl(IProjectService projectService, IUserService userService, ITaskRepository taskRepository) {
        this.projectService = projectService;
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public Task save(@Valid TaskCreateDTO taskCreateDTO) {

        User requester = getUserForCurrentContext();

        Project project = getRequestedProjectForUser(requester, taskCreateDTO);

        if (project == null) {
            throw new AuthenticationFailedException("пользователь не является участником этого проекта");
        }

        User implementer = new User(taskCreateDTO.getImplementer().getUuid());

        project = getRequestedProjectForUser(implementer, taskCreateDTO);

        if (project == null) {
            throw new StructuredException("implementer", "не является участником этого проекта");
        }

        Task toPersist = new Task();

        toPersist.setUuid(UUID.randomUUID());

        updateTaskFiled(taskCreateDTO, project, implementer, toPersist);

        try {

            return taskRepository.saveAndFlush(toPersist);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }

    }

    @Override
    @Transactional
    public Task update(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO) {

        User requester = getUserForCurrentContext();

        Project project = getRequestedProjectForUser(requester, taskCreateDTO);

        if (project == null) {
            throw new AuthenticationFailedException("пользователь не является участником этого проекта");
        }

        User implementer = new User(taskCreateDTO.getImplementer().getUuid());

        project = getRequestedProjectForUser(implementer, taskCreateDTO);

        if (project == null) {
            throw new StructuredException("implementer", "не является участником этого проекта");
        }

        Task toUpdate = taskRepository.findById(uuid).orElseThrow(
                () -> new StructuredException("uuid", "не найдено по такому uuid")
        );

        if (
                !Objects.equals(
                        toUpdate.getDtUpdate(),
                        dtUpdate
                )
        ) {
            throw new StructuredException("dt_update", "Версия проекта уже была обновлена");
        }

        updateTaskFiled(taskCreateDTO, project, implementer, toUpdate);

        try {

            return taskRepository.saveAndFlush(toUpdate);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }

    }

    @Override
    @Transactional
    public Task updateStatus(UUID uuid, LocalDateTime dtUpdate, TaskStatus taskStatus) {

        User requester = getUserForCurrentContext();

        Task toUpdate = taskRepository.findById(uuid).orElseThrow(
                () -> new StructuredException("uuid", "не найдено по такому uuid")
        );

        if (
                !Objects.equals(
                        toUpdate.getDtUpdate(),
                        dtUpdate
                )
        ) {
            throw new StructuredException("dt_update", "Версия проекта уже была обновлена");
        }

        if (!projectService.userIsInProject(requester, toUpdate.getProject().getUuid())) {

            throw new AuthenticationFailedException("пользователь не является участником этого проекта");

        }

        toUpdate.setStatus(taskStatus);

        try {

            return taskRepository.saveAndFlush(toUpdate);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }


    }

    private static void updateTaskFiled(TaskCreateDTO taskCreateDTO, Project project, User implementer, Task toPersist) {
        toPersist.setProject(project);
        toPersist.setTitle(taskCreateDTO.getTitle());
        toPersist.setDescription(taskCreateDTO.getDescription());
        toPersist.setStatus(taskCreateDTO.getStatus());
        toPersist.setImplementer(implementer);
    }


    private User getUserForCurrentContext() {
        User user;

        try {

            user = userService.findUserInCurrentContext();

        } catch (NullPointerException e) {

            throw new AuthenticationFailedException("доступ запрещен");

        }
        return user;
    }


    private Project getRequestedProjectForUser(User user, TaskCreateDTO taskCreateDTO) {

        UUID uuid = taskCreateDTO.getProject().getUuid();

        List<Project> projectsWhereUserIsInProject = projectService.getProjectsWhereUserIsInProject(user);

        for (Project project : projectsWhereUserIsInProject) {

            if (project.getUuid().equals(uuid)) {
                return project;
            }

        }

        return null;

    }

    @Override
    public Task findByUUID(UUID uuid) {
        return null;
    }

    @Override
    public Page<Task> getPage(Integer currentRequestedPage, Integer rowsPerPage) {
        return null;
    }
}
