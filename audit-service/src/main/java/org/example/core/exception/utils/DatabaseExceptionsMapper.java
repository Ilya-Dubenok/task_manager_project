package org.example.core.exception.utils;

import org.example.core.exception.StructuredException;
import org.example.dao.entities.audit.Audit;
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
                exception.put(propertyLimit.getAuditFieldName(), propertyLimit.getMessage());
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


        ;


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
        AUDIT_ID_NOT_NULL(
                Audit.class, "id", "id", "Не задано id для типа записи"),
        AUDIT_TEXT_NOT_NULL(
                Audit.class, "text", "text", "Не задан текст для записи"),
        AUDIT_TYPE_NOT_NULL(
                Audit.class, "type", "type", "Не задан type для записи");





        private final Class<?> entity;

        private final String className;

        private final String propertyName;

        private final String auditFieldName;

        private final String message;


        PropertyLimit(Class<?> entity, String propertyName, String auditFieldName, String propMessage) {
            this.entity = entity;
            this.className = entity.getName();
            this.propertyName = propertyName;
            this.auditFieldName = auditFieldName;
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

        public String getAuditFieldName() {
            return auditFieldName;
        }
    }


}
