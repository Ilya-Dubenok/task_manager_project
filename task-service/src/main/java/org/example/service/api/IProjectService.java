package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.user.User;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IProjectService {

    Project save(@Valid ProjectCreateDTO projectCreateDTO);

    Project update(UUID uuid, LocalDateTime dtUpdate, @Valid ProjectCreateDTO projectCreateDTO);

    Project findByUUID(UUID projectUuid);

    Project findWithRoleOfUserInContextCheck(UUID projectUuid);

    Project findOneForUserInCurrentContext(UUID projectUuid);

    boolean userIsInProject(User user, UUID projectUuid);

    Page<Project> getAllPagesAndShowArchivedIs(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived);

    List<Project> getProjectsWhereUserIsInProject(User user);

    List<Project> getProjectsWhereUserIsInProject(User user, List<UUID> projectUuidsList);

    Page<Project> getPageWhereUserInContextWorksAndShowArchived(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived);

    Page<Project> getPageWithUserRoleInContextCheckAndShowArchived(Integer page, Integer size, Boolean showArchived);
}
