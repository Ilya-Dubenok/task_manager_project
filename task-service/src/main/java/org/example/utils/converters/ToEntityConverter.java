package org.example.utils.converters;

import org.example.core.dto.project.ProjectCreateDTO;
import org.example.core.dto.task.TaskCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.user.User;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ToEntityConverter implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(UserDTO.class, User.class),
                new ConvertiblePair(ProjectCreateDTO.class, Project.class),
                new ConvertiblePair(TaskCreateDTO.class, Task.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        Class<?> expectedSourceClass = sourceType.getType();

        Class<?> expectedTargetClass = targetType.getType();

        if (expectedSourceClass.equals(UserDTO.class) && expectedTargetClass.equals(User.class)) {

            User res = new User();
            UserDTO userDTO = (UserDTO) source;
            res.setUuid(userDTO.getUuid());
            return res;

        }

        if (expectedSourceClass.equals(ProjectCreateDTO.class) && expectedTargetClass.equals(Project.class)) {

            Project res = new Project();
            ProjectCreateDTO projectCreateDTO = (ProjectCreateDTO) source;

            res.setName(projectCreateDTO.getName());
            res.setDescription(projectCreateDTO.getDescription());
            res.setStatus(projectCreateDTO.getStatus());

            return res;

        }

        if (expectedSourceClass.equals(TaskCreateDTO.class) && expectedTargetClass.equals(Task.class)) {

            Task res = new Task();
            TaskCreateDTO taskCreateDTO  = (TaskCreateDTO) source;

            res.setTitle(taskCreateDTO.getTitle());
            res.setDescription(taskCreateDTO.getDescription());

            return res;

        }

        return null;
    }
}
