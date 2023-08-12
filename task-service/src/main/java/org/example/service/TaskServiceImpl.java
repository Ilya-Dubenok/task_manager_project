package org.example.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

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
    public Task findByUUID(UUID taskUuid) {

        return taskRepository.findById(taskUuid).orElseThrow(
                () -> new StructuredException("uuid", "не найдено по такому uuid")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Task findByUUIDForUserInContext(UUID taskUuid) {

        User requester = getUserForCurrentContext();

        Task task = findByUUID(taskUuid);

        if (!projectService.userIsInProject(requester, task.getProject().getUuid())) {

            return null;

        }

        return task;

    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> getPageWithFilters(Integer currentRequestedPage, Integer rowsPerPage, List<UUID> projectUuids, List<UUID> implementersUuids, List<TaskStatus> taskStatuses) {

        User requester = getUserForCurrentContext();

        List<Project> projectsToFilter;

        if (projectUuids != null && projectUuids.size() != 0) {

            projectsToFilter = projectService.getProjectsWhereUserIsInProject(requester, projectUuids);

        } else {

            projectsToFilter = projectService.getProjectsWhereUserIsInProject(requester);

        }

        if (projectsToFilter.size() == 0) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(currentRequestedPage, rowsPerPage), 0);
        }

        return taskRepository.findAll(
                getTaskSpecificationOnFilters(projectsToFilter, implementersUuids, taskStatuses),
                PageRequest.of(currentRequestedPage, rowsPerPage, Sort.by("uuid"))
        );


    }

    @Override
    @Transactional
    public Task save(TaskCreateDTO taskCreateDTO) {


        UserDTO implementerDTO = taskCreateDTO.getImplementer();

        User implementer = null;

        if (implementerDTO != null) {

            implementer = new User(implementerDTO.getUuid());

        }

        Project project = getProjectAndCheckImplementer(taskCreateDTO, implementer);

        Task toPersist = new Task();

        toPersist.setUuid(UUID.randomUUID());

        updateTaskFields(taskCreateDTO, project, implementer, toPersist);

        try {

            return taskRepository.saveAndFlush(toPersist);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }

    }

    @Override
    @Transactional
    public Task saveWithUserRoleInContextCheck(TaskCreateDTO taskCreateDTO) {

        UUID projectUuid = taskCreateDTO.getProject().getUuid();

        if (!projectService.projectExists(projectUuid)) {
            throw new StructuredException("project", "не найден такой проект");
        }

        if (userService.userInCurrentContextHasOneOfRoles(UserRole.ADMIN)) {

            return save(taskCreateDTO);

        } else {

            return saveForUserInContext(taskCreateDTO);

        }
    }

    @Override
    @Transactional
    public Task saveForUserInContext(@Valid TaskCreateDTO taskCreateDTO) {

        User requester = getUserForCurrentContext();

        Project project = getRequestedProjectForUser(requester, taskCreateDTO);

        if (project == null) {
            throw new AuthenticationFailedException("пользователь не является участником этого проекта");
        }

        User implementer = new User(taskCreateDTO.getImplementer().getUuid());

        project = getProjectAndCheckImplementer(taskCreateDTO, implementer);

        Task toPersist = new Task();

        toPersist.setUuid(UUID.randomUUID());

        updateTaskFields(taskCreateDTO, project, implementer, toPersist);

        try {

            return taskRepository.saveAndFlush(toPersist);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }

    }

    @Override
    public Task findWithRoleOfUserInContextCheck(UUID taskUuid) {
        if (userService.userInCurrentContextHasOneOfRoles(UserRole.ADMIN)) {

            return findByUUID(taskUuid);

        } else {

            return findByUUIDForUserInContext(taskUuid);

        }
    }

    @Override
    @Transactional
    public Task update(UUID uuid, LocalDateTime dtUpdate, TaskCreateDTO taskCreateDTO) {

        UserDTO implementerDTO = taskCreateDTO.getImplementer();

        User implementer = null;

        if (implementerDTO != null) {

            implementer = new User(implementerDTO.getUuid());

        }

        Project project = getProjectAndCheckImplementer(taskCreateDTO, implementer);

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

        updateTaskFields(taskCreateDTO, project, implementer, toUpdate);

        try {

            return taskRepository.saveAndFlush(toUpdate);

        } catch (Exception e) {

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);

        }
    }

    @Override
    @Transactional
    public Task updateForUserInContext(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO) {

        User requester = getUserForCurrentContext();

        Project project = getRequestedProjectForUser(requester, taskCreateDTO);

        if (project == null) {
            throw new AuthenticationFailedException("пользователь не является участником этого проекта");
        }

        return update(uuid, dtUpdate, taskCreateDTO);

    }

    @Override
    public Task updateWithRoleOfUserInContextCheck(UUID uuid, LocalDateTime dtUpdate, TaskCreateDTO taskCreateDTO) {
        UUID projectUuid = taskCreateDTO.getProject().getUuid();

        if (!projectService.projectExists(projectUuid)) {
            throw new StructuredException("project", "не найден такой проект");
        }

        if (userService.userInCurrentContextHasOneOfRoles(UserRole.ADMIN)) {

            return update(uuid, dtUpdate, taskCreateDTO);

        } else {

            return updateForUserInContext(uuid, dtUpdate, taskCreateDTO);

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

    private Project getProjectAndCheckImplementer(TaskCreateDTO taskCreateDTO, User implementer) {
        Project project = getRequestedProjectForUser(implementer, taskCreateDTO);

        if (project == null) {
            throw new StructuredException("implementer", "не является участником этого проекта");
        }
        return project;
    }

    private static void validatePageArguments(Integer currentRequestedPage, Integer rowsPerPage) {
        StructuredException exception = new StructuredException();

        if (currentRequestedPage < 0) {

            exception.put("page", "Номер страницы не может быть меньше 0");

        }
        if (rowsPerPage < 1) {
            exception.put("size", "Размер страницы не может быть меньше 0");

        }

        if (exception.hasExceptions()) {
            throw exception;
        }
    }

    private static void updateTaskFields(TaskCreateDTO taskCreateDTO, Project project, User implementer, Task toPersist) {
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

        UUID projectUuid = taskCreateDTO.getProject().getUuid();


        if (user == null) {

            return projectService.findByUUID(projectUuid);

        }

        List<Project> projectsWhereUserIsInProject = projectService.getProjectsWhereUserIsInProject(user);

        for (Project project : projectsWhereUserIsInProject) {

            if (project.getUuid().equals(projectUuid)) {
                return project;
            }

        }

        return null;

    }

    private static Specification<Task> getTaskSpecificationOnFilters(List<Project> projectsToFilter, List<UUID> implementersUuids, List<TaskStatus> taskStatuses) {
        return (root, query, builder) -> {

            Path<Object> project = root.get("project");
            CriteriaBuilder.In<Object> inProject = builder.in(project);
            Predicate res = inProject.value(projectsToFilter);

            if (taskStatuses != null && taskStatuses.size() != 0) {

                Path<Object> status = root.get("status");
                CriteriaBuilder.In<Object> inStatus = builder.in(status);
                Predicate inListOfStatuses = inStatus.value(taskStatuses);
                res = builder.and(res, inListOfStatuses);

            }

            if (implementersUuids != null && implementersUuids.size() != 0) {

                Path<Object> implementerUuid = root.get("implementer").get("uuid");
                CriteriaBuilder.In<Object> inImplementer = builder.in(implementerUuid);
                Predicate inListOfImplementers = inImplementer.value(implementersUuids);
                res = builder.and(res, inListOfImplementers);

            }

            return res;

        };
    }
}
