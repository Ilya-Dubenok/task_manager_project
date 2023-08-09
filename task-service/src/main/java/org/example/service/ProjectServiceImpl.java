package org.example.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IProjectRepository;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.project.ProjectStatus;
import org.example.dao.entities.user.User;
import org.example.service.api.IProjectService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Validated
@Service
public class ProjectServiceImpl implements IProjectService {

    private IUserService userService;

    private IProjectRepository projectRepository;

    private ConversionService conversionService;

    public ProjectServiceImpl(IUserService userService, IProjectRepository projectRepository,
                              ConversionService conversionService) {
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.conversionService = conversionService;
    }

    @Override
    @Transactional
    public Project save(@Valid ProjectCreateDTO projectCreateDTO) {

        Project toPersist = conversionService.convert(projectCreateDTO, Project.class);
        toPersist.setUuid(UUID.randomUUID());

        verifyAndPersistUsers(projectCreateDTO, toPersist);

        try {

            return projectRepository.saveAndFlush(toPersist);


        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE);
        }

    }



    @Override
    @Transactional(readOnly = true)
    public Project findByUUID(UUID uuid) {

        return projectRepository.findById(uuid).orElseThrow(
                () -> new StructuredException("uuid", "Не найден проект по такому id")
        );

    }

    //TODO ADD SEARCH FOR USER
    @Override
    @Transactional(readOnly = true)
    public Page<Project> getPage(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived) {

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

        Page<Project> page = projectRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

        return page;
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

    private void assignTeam(UserDTO manager, Set<UserDTO> staff, Project toPersist) {


    }

    private void verifyAndPersistUsers(ProjectCreateDTO projectCreateDTO, Project project) {

        UserDTO managerDTO = projectCreateDTO.getManager();

        if (managerDTO != null) {

            try {

                User manager = userService.findByRoleAndSave(managerDTO, UserRole.MANAGER);
                project.setManager(manager);

            } catch (ConstraintViolationException e) {

                throw new StructuredException("manager", e.getMessage());

            }
        }

        Set<UserDTO> staff = projectCreateDTO.getStaff();

        if (staff != null) {

            try {

                List<User> staffOfUsers = userService.findAllAndSave(staff);
                project.setStaff(new HashSet<>(staffOfUsers));

            } catch (ConstraintViolationException e) {

                throw new StructuredException("staff", e.getMessage());

            }
        }

    }
}
