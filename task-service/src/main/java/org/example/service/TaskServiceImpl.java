package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.exception.AuthenticationFailedException;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.ITaskRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.ITaskService;
import org.example.service.api.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
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

        User user;

        try {

            user = userService.findUserInCurrentContext();

        } catch (NullPointerException e) {

            throw new AuthenticationFailedException("доступ запрещен");

        }

        UUID projectUuid = taskCreateDTO.getProject().getUuid();

        Project project = getRequestedProjectForUser(user, projectUuid);

        if (project == null) {
            throw new AuthenticationFailedException("пользователь не является участником этого проекта");
        }


        User implementer = new User(taskCreateDTO.getImplementer().getUuid());

        project = getRequestedProjectForUser(implementer, projectUuid);

        if (project == null) {
            throw new StructuredException("implementer", "не является участником этого проекта");
        }

        Task toPersist = new Task();

        toPersist.setUuid(UUID.randomUUID());
        toPersist.setProject(project);
        toPersist.setTitle(taskCreateDTO.getTitle());
        toPersist.setDescription(taskCreateDTO.getDescription());
        toPersist.setStatus(taskCreateDTO.getStatus());
        toPersist.setImplementer(implementer);

        try {

            return taskRepository.saveAndFlush(toPersist);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }

    }


    private Project getRequestedProjectForUser(User user, UUID projectUuid) {

        List<Project> projectsWhereUserIsInProject = projectService.getProjectsWhereUserIsInProject(user);

        for (Project project : projectsWhereUserIsInProject) {

            if (project.getUuid().equals(projectUuid)) {
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
