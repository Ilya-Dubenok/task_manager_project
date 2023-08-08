package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.dao.entities.task.Task;
import org.example.service.api.ITaskService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
@Service
public class TaskServiceImpl implements ITaskService {
    @Override
    public void save(@Valid TaskCreateDTO taskCreateDTO) {

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
