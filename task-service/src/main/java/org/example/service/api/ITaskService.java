package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.springframework.data.domain.Page;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ITaskService {

    Task save(@Valid TaskCreateDTO taskCreateDTO);

    Task saveForUserInContext(@Valid TaskCreateDTO taskCreateDTO);

    Task update(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO);

    Task updateForUserInContext(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO);

    Task updateWithRoleOfUserInContextCheck(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO);

    Task updateStatus(UUID uuid, LocalDateTime dtUpdate, TaskStatus taskStatus);

    Task updateStatusForUserInContext(UUID uuid, LocalDateTime dtUpdate, TaskStatus taskStatus);

    Task updateStatusWithRoleOfUserInContextCheck(UUID uuid, LocalDateTime dtUpdate, TaskStatus taskStatus);

    Task findByUUID(UUID taskUuid);

    Task findByUUIDForUserInContext(UUID taskUuid);

    Task findWithRoleOfUserInContextCheck(UUID taskUuid);

    Page<Task> getPageWithFilters(Integer currentRequestedPage, Integer rowsPerPage, List<UUID> projectUuids,
                                  List<UUID> implementersUuids, List<TaskStatus> taskStatuses);

    Task saveWithUserRoleInContextCheck(@Valid TaskCreateDTO taskCreateDTO);
}
