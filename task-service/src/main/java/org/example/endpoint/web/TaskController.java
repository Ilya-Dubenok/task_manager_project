package org.example.endpoint.web;


import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.task.TaskDTO;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.example.service.api.ITaskService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final ITaskService taskService;

    private final ConversionService conversionService;


    public TaskController(ITaskService taskService, ConversionService conversionService) {
        this.taskService = taskService;
        this.conversionService = conversionService;
    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<TaskDTO> getByUuid(@PathVariable UUID uuid) {

        Task task = taskService.findWithRoleOfUserInContextCheck(uuid);

        TaskDTO dto = conversionService.convert(task, TaskDTO.class);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaskCreateDTO taskCreateDTO) {

        taskService.saveWithUserRoleInContextCheck(taskCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PutMapping(value = "/{uuid}/dt_update/{dt_update}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                    @RequestBody TaskCreateDTO taskCreateDTO) {

        taskService.updateWithRoleOfUserInContextCheck(uuid, dt_update, taskCreateDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }



    @PatchMapping(value = "/{uuid}/dt_update/{dt_update}/status/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                          @PathVariable TaskStatus status) {

        taskService.updateStatusWithRoleOfUserInContextCheck(uuid, dt_update, status);

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
