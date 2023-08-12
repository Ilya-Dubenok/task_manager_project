package org.example.endpoint.web;


import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.task.TaskDTO;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.task.TaskStatus;
import org.example.service.api.ITaskService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @GetMapping
    public ResponseEntity<?> getPage(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                     @RequestParam(value = "size", defaultValue = "20") Integer size,
                                     @RequestParam(value = "project", required = false)List<UUID> projectUuids,
                                     @RequestParam(value = "implementer", required = false)List<UUID> implementerUuids,
                                     @RequestParam(value = "status", required = false)List<TaskStatus> statuses
                                     ){

        Page<Task> pages = taskService.getPagesWithRoleOfUserInContextCheck(page, size, projectUuids, implementerUuids, statuses);
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                PageOfTypeDTO.class, TaskDTO.class
        );

        Object converted = conversionService.convert(
                pages, TypeDescriptor.valueOf(PageImpl.class),
                new TypeDescriptor(resolvableType, null, null)
        );

        return new ResponseEntity<>(converted, HttpStatus.OK);

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
