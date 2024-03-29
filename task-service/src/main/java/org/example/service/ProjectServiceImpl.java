package org.example.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.example.core.dto.audit.Type;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.AuthenticationFailedException;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IProjectRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IAuditService;
import org.example.service.api.IProjectService;
import org.example.service.api.IUserService;
import org.example.service.utils.JsonAuditMessagesFormer;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
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
public class ProjectServiceImpl implements IProjectService {

    private IUserService userService;

    private IProjectRepository projectRepository;

    private ConversionService conversionService;

    private JsonAuditMessagesFormer auditMessagesFormer;

    private IAuditService auditService;

    public ProjectServiceImpl(IUserService userService,
                              IProjectRepository projectRepository,
                              ConversionService conversionService,
                              JsonAuditMessagesFormer auditMessagesFormer,
                              IAuditService auditService) {
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.conversionService = conversionService;
        this.auditMessagesFormer = auditMessagesFormer;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public Project save(@Valid ProjectCreateDTO projectCreateDTO) {

        Project toPersist = conversionService.convert(projectCreateDTO, Project.class);

        toPersist.setUuid(UUID.randomUUID());

        verifyAndPersistUsers(projectCreateDTO, toPersist);

        Project res;

        try {

             res = projectRepository.saveAndFlush(toPersist);


        } catch (Exception e) {
            StructuredException structuredException = new StructuredException();

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        try {

            auditService.sendAudit(
                    userService.findUserInCurrentContext().getUuid(),
                    auditMessagesFormer.formObjectCreatedAuditMessage(res),
                    Type.PROJECT,
                    res.getUuid().toString()
            );

        } catch (Exception ignored) {
            //TODO logging
        }


        return res;

    }

    @Override
    @Transactional
    public Project update(UUID uuid, LocalDateTime dtUpdate, ProjectCreateDTO projectCreateDTO) {


        Project toUpdate = projectRepository.findById(uuid).orElseThrow(
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

        Project copyBeforeUpdate = copyProject(toUpdate);

        updateProjectFields(projectCreateDTO, toUpdate);

        if (Objects.equals(copyBeforeUpdate, toUpdate)){

            return toUpdate;

        }

        try {

            toUpdate =  projectRepository.saveAndFlush(toUpdate);

        } catch (Exception e) {

            StructuredException structuredException = new StructuredException();

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        try {

            if (!copyBeforeUpdate.equals(toUpdate)) {
                auditService.sendAudit(
                        userService.findUserInCurrentContext().getUuid(),
                        auditMessagesFormer.formObjectUpdatedAuditMessage(copyBeforeUpdate, toUpdate),
                        Type.PROJECT,
                        toUpdate.getUuid().toString()
                );
            }

        } catch (Exception ignored) {
            //TODO logging
        }

        return toUpdate;

    }


    @Override
    @Transactional(readOnly = true)
    public Project findByUUID(UUID projectUuid) {

        return projectRepository.findById(projectUuid).orElseThrow(
                () -> new StructuredException("uuid", "Не найден проект по такому id")
        );

    }

    @Override
    @Transactional(readOnly = true)
    public Project findWithRoleOfUserInContextCheck(UUID projectUuid) {
        if (userService.userInCurrentContextHasOneOfRoles(UserRole.ADMIN)) {

            return projectRepository.findById(projectUuid).orElseThrow(
                    () -> new StructuredException("uuid", "Не найден проект по такому id")
            );

        } else {

            return this.findOneForUserInCurrentContext(projectUuid);

        }
    }

    @Override
    @Transactional(readOnly = true)
    public Project findOneForUserInCurrentContext(UUID projectUuid) {


        User userInCurrentContext;

        try {

            userInCurrentContext = userService.findUserInCurrentContext();

        } catch (NullPointerException e) {

            throw new AuthenticationFailedException("неавторизированный доступ");

        }

        Project found = findByUUID(projectUuid);

        User manager = found.getManager();

        if (manager != null && userInCurrentContext.getUuid().equals(manager.getUuid())) {

            return found;

        }

        Set<User> staff = found.getStaff();

        if (staff != null) {

            for (User worker : staff) {

                if (worker.getUuid().equals(userInCurrentContext.getUuid())) {
                    return found;
                }

            }
        }


        return null;

    }

    @Override
    @Transactional(readOnly = true)
    public boolean userIsInProject(User user, UUID projectUuid) {
        return projectRepository.exists(getSpecificationOfProjectUuidAndUserIsInProject(user, projectUuid));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean projectExists(UUID projectUuid) {
        return projectRepository.existsById(projectUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsWhereUserIsInProject(User user) {

        return projectRepository.findAll(((root, query, builder) -> {
                    Predicate isManager = builder.equal(root.get("manager"), user);
                    Predicate isInStuff = builder.isMember(user, root.get("staff"));

                    return builder.or(isManager, isInStuff);
                })
        );

    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsWhereUserIsInProject(User user, List<UUID> projectUuidsList) {
        return projectRepository.findAll(((root, query, builder) -> {

                    Path<Object> uuidPath = root.get("uuid");

                    CriteriaBuilder.In<Object> in = builder.in(uuidPath);

                    Predicate inUuidListPredicate = in.value(projectUuidsList);

                    Predicate isManager = builder.equal(root.get("manager"), user);

                    Predicate isInStuff = builder.isMember(user, root.get("staff"));

                    return builder.and(inUuidListPredicate, builder.or(isManager, isInStuff));
                })
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getPageWithUserRoleInContextCheckAndShowArchived(Integer page, Integer size, Boolean showArchived) {

        validatePageArguments(page, size);

        if (userService.userInCurrentContextHasOneOfRoles(UserRole.ADMIN)) {

            return getAllPagesAndShowArchivedIs(page, size, showArchived);

        } else {

            return getPageWhereUserInContextWorksAndShowArchived(page, size, showArchived);

        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> getPageWhereUserInContextWorksAndShowArchived(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived) {


        User userInCurrentContext;

        try {

            userInCurrentContext = userService.findUserInCurrentContext();

        } catch (NullPointerException e) {

            throw new AuthenticationFailedException("неавторизированный доступ");

        }

        return projectRepository.findAll(
                getSpecificationOfUserIsInProjectAndShowArchivedIs(userInCurrentContext, showArchived),
                PageRequest.of(currentRequestedPage, rowsPerPage, Sort.by("uuid"))
        );


    }


    @Override
    @Transactional(readOnly = true)
    public Page<Project> getAllPagesAndShowArchivedIs(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived) {

        if (showArchived) {

            return projectRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

        } else {

            return projectRepository.findAll(
                    ((root, query, builder) -> builder.or(
                            builder.notEqual(root.get("status"), ProjectStatus.ARCHIVED),
                            builder.isNull(root.get("status"))
                    ))
                    , PageRequest.of(currentRequestedPage, rowsPerPage, Sort.by("uuid"))
            );

        }

    }

    private Specification<Project> getSpecificationOfProjectUuidAndUserIsInProject(User worksInProject, UUID projectUuid) {

        return (root, query, builder) -> {

            Predicate res = builder.equal(root.get("uuid"), projectUuid);

            Predicate isManager = builder.equal(root.get("manager"), worksInProject);

            Predicate isInStuff = builder.isMember(worksInProject, root.get("staff"));

            res = builder.and(res, builder.or(isManager, isInStuff));

            return res;
        };


    }

    private Specification<Project> getSpecificationOfUserIsInProjectAndShowArchivedIs(User user, Boolean showArchived) {
        return (root, query, builder) -> {

            Predicate isManager = builder.equal(root.get("manager"), user);
            Predicate isInStuff = builder.isMember(user, root.get("staff"));

            Predicate res = builder.or(isManager, isInStuff);

            if (showArchived) {
                return res;
            }

            Predicate isProjectArchived = builder.or(
                    builder.notEqual(root.get("status"), ProjectStatus.ARCHIVED),
                    builder.isNull(root.get("status"))
            );

            res = builder.and(res, isProjectArchived);

            return res;
        };
    }

    private Specification<Project> getSpecificationOfUuidAndUserIsInProjectAndShowArchivedIs(UUID projectUuid, User user, Boolean showArchived) {

        return (root, query, builder) -> {

            Predicate uuidMatches = builder.equal(root.get("uuid"), projectUuid);

            Predicate res = builder.and(uuidMatches,
                    getSpecificationOfUserIsInProjectAndShowArchivedIs(user, showArchived).toPredicate(root, query, builder)
            );

            return res;

        };

    }


    private Project copyProject(Project toUpdate) {

        Project res = new Project(
                toUpdate.getUuid(),
                toUpdate.getDtCreate(),
                toUpdate.getDtUpdate(),
                toUpdate.getName(),
                toUpdate.getDescription(),
                toUpdate.getManager(),
                toUpdate.getStaff(),
                toUpdate.getStatus()
        );

        return res;

    }

    private void verifyAndPersistUsers(ProjectCreateDTO projectCreateDTO, Project project) {

        UserDTO managerDTO = projectCreateDTO.getManager();

        StructuredException allChecksResultException = new StructuredException();

        if (managerDTO != null) {

            try {

                User manager = userService.findByRoleAndSave(managerDTO, UserRole.MANAGER);

                project.setManager(manager);

            } catch (ConstraintViolationException e) {

                allChecksResultException.put("manager", e.getMessage());

            }
        } else {
            project.setManager(null);
        }

        Set<UserDTO> staff = projectCreateDTO.getStaff();

        if (staff != null && staff.size() != 0) {

            try {

                List<User> staffOfUsers = userService.findAllAndSave(staff);

                project.setStaff(new HashSet<>(staffOfUsers));

            } catch (ConstraintViolationException e) {

                allChecksResultException.put("staff", e.getMessage());

            }
        } else {

            project.setStaff(null);

        }

        if (allChecksResultException.hasExceptions()) {

            throw allChecksResultException;

        }

    }

    private void updateProjectFields(ProjectCreateDTO projectCreateDTO, Project toUpdate) {

        verifyAndPersistUsers(projectCreateDTO, toUpdate);

        toUpdate.setName(projectCreateDTO.getName());

        toUpdate.setDescription(projectCreateDTO.getDescription());

        toUpdate.setStatus(projectCreateDTO.getStatus());

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
}
