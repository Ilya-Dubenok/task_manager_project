package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.UUID;

public interface ITaskService {

    Task save(@Valid TaskCreateDTO taskCreateDTO);

    @Transactional
    Task update(UUID uuid, LocalDateTime dtUpdate, @Valid TaskCreateDTO taskCreateDTO);

    Task updateStatus(UUID uuid, LocalDateTime dtUpdate, TaskStatus taskStatus);

    Task findByUUID(UUID uuid);

    Page<Task> getPage(Integer currentRequestedPage, Integer rowsPerPage);


}
