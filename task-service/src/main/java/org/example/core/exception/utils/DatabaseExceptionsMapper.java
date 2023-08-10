package org.example.core.exception.utils;

import org.example.core.exception.StructuredException;
import org.example.dao.entities.task.Task;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;

public class DatabaseExceptionsMapper {


    public static boolean isExceptionCauseRecognized(Throwable cause, StructuredException exception) {


        if (cause instanceof ConstraintViolationException) {
            String constraintName = ((ConstraintViolationException) cause).getConstraintName();
            Constraint constraint = findProperConstraint(constraintName);

            if (constraint != null) {
                exception.put(
                        constraint.getFiledName(), constraint.getConstraintMessage()
                );
                return true;

            }
            return false;

        } else if (cause instanceof PropertyValueException) {

            String entityName = ((PropertyValueException) cause).getEntityName();
            String propertyName = ((PropertyValueException) cause).getPropertyName();
            PropertyLimit propertyLimit = findPropertyLimit(entityName, propertyName);
            if (propertyLimit != null) {
                exception.put(propertyLimit.getDTOFieldName(), propertyLimit.getMessage());
                return true;

            }
            return false;


        } else {

            Throwable innerCause = cause.getCause();
            if (innerCause == null || innerCause == cause) {
                return false;
            }

            return isExceptionCauseRecognized(innerCause, exception);
        }


    }

    private static Constraint findProperConstraint(String constraintName) {
        if (constraintName == null) {
            return null;
        }
        for (Constraint value : Constraint.values()) {
            if (value.getConstraintName().equals(constraintName)) {
                return value;
            }


        }
        return null;

    }

    public static PropertyLimit findPropertyLimit(String entityName, String propertyName) {

        if (entityName == null || propertyName == null) {
            return null;
        }

        for (PropertyLimit value : PropertyLimit.values()) {

            if (value.getClassName().equals(entityName) && value.getPropertyName().equals(propertyName)) {
                return value;

            }
        }
        return null;

    }


    private enum Constraint {

        PROJECT_NAME_UNIQUE_CONSTRAINT("project_name_unique_constraint", "name",
                "задано не уникальное название проекта"),
        NO_MANAGER_FOUND_CONSTRAINT("project_users_foreign_key", "manager",
                "данного менеджера не существует"),
        NO_PROJECT_FOR_TASK_EXISTS("task_project_foreign_key", "project",
                "Введен uuid не существующего проекта"),
        NO_IMPLEMENTER_FOR_TASK_EXISTS("task_user_foreign_key", "implementer", "Введен uuid не" +
                " существующего пользователя");


        private final String constraintName;

        private final String filedName;

        private final String constraintMessage;

        Constraint(String constraintName, String filedName, String constraintMessage) {
            this.constraintName = constraintName;
            this.filedName = filedName;
            this.constraintMessage = constraintMessage;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public String getConstraintMessage() {
            return constraintMessage;
        }

        public String getFiledName() {
            return filedName;
        }
    }

    private enum PropertyLimit {

        TASK_TITLE_NOT_NULL(
                Task.class, "title", "title", "Не задано название проекта");


        private final Class<?> entity;

        private final String className;

        private final String propertyName;

        private final String DTOfieldName;

        private final String message;


        PropertyLimit(Class<?> entity, String propertyName, String DTOfieldName, String propMessage) {
            this.entity = entity;
            this.className = entity.getName();
            this.propertyName = propertyName;
            this.DTOfieldName = DTOfieldName;
            this.message = propMessage;
        }

        public String getClassName() {
            return className;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getMessage() {
            return message;
        }

        public Class<?> getEntity() {
            return entity;
        }

        public String getDTOFieldName() {
            return DTOfieldName;
        }
    }


}
