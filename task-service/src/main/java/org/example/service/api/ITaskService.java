package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.dao.entities.task.Task;
import org.springframework.data.domain.Page;


import java.util.UUID;

public interface ITaskService {

    void save(@Valid TaskCreateDTO taskCreateDTO);

    Task findByUUID(UUID uuid);

    Page<Task> getPage(Integer currentRequestedPage, Integer rowsPerPage);


}
