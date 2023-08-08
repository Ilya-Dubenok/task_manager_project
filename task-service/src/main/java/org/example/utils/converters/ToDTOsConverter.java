package org.example.utils.converters;

import org.example.core.dto.project.ProjectDTO;
import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.task.TaskDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.user.User;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ToDTOsConverter<IN, OUT> implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(User.class, UserDTO.class),
                new ConvertiblePair(Project.class, ProjectDTO.class),
                new ConvertiblePair(Task.class, TaskDTO.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        Class<?> expectedSourceClass = sourceType.getType();

        Class<?> expectedTargetClass = targetType.getType();

        if (expectedSourceClass.equals(User.class) && expectedTargetClass.equals(UserDTO.class)) {

            UserDTO res = new UserDTO();
            User user = (User) source;
            res.setUuid(user.getUuid());
            return res;

        }

        if (expectedSourceClass.equals(Project.class) && expectedTargetClass.equals(ProjectDTO.class)) {

            ProjectDTO res = new ProjectDTO();
            Project project = (Project) source;

            res.setUuid(project.getUuid());
            res.setDtCreate(
                    ZonedDateTime.of(project.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setDtUpdate(
                    ZonedDateTime.of(project.getDtUpdate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setName(project.getName());
            res.setDescription(project.getDescription());
            res.setManager(fromUserToUserDTOConverter(project.getManager()));
            res.setStaff(
                    project.getStaff().stream()
                            .map(this::fromUserToUserDTOConverter)
                            .collect(Collectors.toSet())
            );
            res.setStatus(project.getStatus());
            return res;

        }

        if (expectedSourceClass.equals(Task.class) && expectedTargetClass.equals(TaskDTO.class)) {
            TaskDTO res = new TaskDTO();
            Task task = (Task) source;

            res.setUuid(task.getUuid());
            res.setDtCreate(
                    ZonedDateTime.of(task.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setDtUpdate(
                    ZonedDateTime.of(task.getDtUpdate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setProject(new ProjectUuidDTO(task.getProject().getUuid()));
            res.setTitle(task.getTitle());
            res.setDescription(task.getDescription());
            res.setStatus(task.getStatus());
            res.setImplementer(fromUserToUserDTOConverter(task.getImplementer()));

            return res;

        }

        return null;

    }

    private UserDTO fromUserToUserDTOConverter(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getUuid());
    }


}
