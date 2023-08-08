package org.example.utils.converters;

import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.project.ProjectDTO;
import org.example.core.dto.project.ProjectUuidDTO;
import org.example.core.dto.task.TaskDTO;
import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.project.Project;
import org.example.dao.entities.task.Task;
import org.example.dao.entities.user.User;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ToDTOsConverter<IN, OUT> implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(User.class, UserDTO.class),
                new ConvertiblePair(Project.class, ProjectDTO.class),
                new ConvertiblePair(Task.class, TaskDTO.class),
                new ConvertiblePair(Page.class, PageOfTypeDTO.class)
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

        if (expectedSourceClass.equals(PageImpl.class) && expectedTargetClass.equals(PageOfTypeDTO.class)) {

            Type resolvedGenericType = targetType.getResolvableType().getGeneric(0).getType();

            if (resolvedGenericType.equals(ProjectDTO.class)) {

                PageOfTypeDTO<ProjectDTO> res = new PageOfTypeDTO<>();

                Page<Project> projectPage = (Page<Project>) source;

                List<ProjectDTO> content = new ArrayList<>();
                for (Project project : projectPage.toList()) {
                    content.add(
                            (ProjectDTO) this.convert(project,
                                    TypeDescriptor.valueOf(Project.class),
                                    TypeDescriptor.valueOf(ProjectDTO.class))
                    );

                }

                fillPageWithValues(res, projectPage, content);

                return res;
            }

            if (resolvedGenericType.equals(TaskDTO.class)) {
                
                PageOfTypeDTO<TaskDTO> res = new PageOfTypeDTO<>();

                Page<Task> projectPage = (Page<Task>) source;

                List<TaskDTO> content = new ArrayList<>();
                for (Task project : projectPage.toList()) {
                    content.add(
                            (TaskDTO) this.convert(project,
                                    TypeDescriptor.valueOf(Task.class),
                                    TypeDescriptor.valueOf(TaskDTO.class))
                    );

                }

                fillPageWithValues(res, projectPage, content);

                return res;

            }


        }

        return null;

    }

    private UserDTO fromUserToUserDTOConverter(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getUuid());
    }

    private <T, E> void fillPageWithValues(PageOfTypeDTO<T> res, Page<E> source, List<T> content) {

        res.setNumber(source.getNumber());
        res.setTotalPages(source.getTotalPages());
        res.setTotalElements(source.getTotalElements());
        res.setFirst(source.isFirst());
        res.setLast(!source.hasNext());
        res.setSize(source.getSize());
        res.setNumberOfElements(content.size());
        res.setContent(content);

    }


}
