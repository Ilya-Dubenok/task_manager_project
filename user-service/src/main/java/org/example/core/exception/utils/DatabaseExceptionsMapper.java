package org.example.core.exception.utils;

import org.example.core.exception.StructuredException;
import org.example.dao.entities.user.User;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;

public class DatabaseExceptionsMapper {


    public static boolean isExceptionCauseRecognized(Throwable cause, StructuredException exception) {


        if (cause instanceof ConstraintViolationException) {
            String constraintName = ((ConstraintViolationException) cause).getConstraintName();
            Constraint constraint = findProperConstraint(constraintName);

            if (constraint != null) {
                exception.put(
                        constraint.getUserFieldName(), constraint.getConstraintMessage()
                );
                return true;

            }
            return false;

        } else if (cause instanceof PropertyValueException) {

            String entityName = ((PropertyValueException) cause).getEntityName();
            String propertyName = ((PropertyValueException) cause).getPropertyName();
            PropertyLimit propertyLimit = findPropertyLimit(entityName, propertyName);
            if (propertyLimit != null) {
                exception.put(propertyLimit.getUserFieldName(), propertyLimit.getMessage());
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


        USER_UNIQUE_MAIL_CONSTRAINT(
                "user_mail_unique_constraint",
                "mail", "введеное значение электронной почты уже используется"

        );


        private final String constraintName;

        private final String userFieldName;

        private final String constraintMessage;

        Constraint(String constraintName, String userFieldName, String constraintMessage) {
            this.constraintName = constraintName;
            this.userFieldName = userFieldName;
            this.constraintMessage = constraintMessage;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public String getConstraintMessage() {
            return constraintMessage;
        }

        public String getUserFieldName() {
            return userFieldName;
        }
    }

    private enum PropertyLimit {
        USER_ROLE_NOT_NULL(
                User.class, "role", "role", "Не задана роль для юзера"),
        USER_STATUS_NOT_NULL(
                User.class, "status", "status", "Не задан статус для юзера");


        private final Class<?> entity;

        private final String className;

        private final String propertyName;

        private final String userFieldName;

        private final String message;


        PropertyLimit(Class<?> entity, String propertyName, String userFieldName, String propMessage) {
            this.entity = entity;
            this.className = entity.getName();
            this.propertyName = propertyName;
            this.userFieldName = userFieldName;
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

        public String getUserFieldName() {
            return userFieldName;
        }
    }


}
