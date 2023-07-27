package org.example.core.exception;

public class SingleFieldAndMessageExceptionInfoDTO {

    private String field;

    private String message;

    public SingleFieldAndMessageExceptionInfoDTO() {
    }

    public SingleFieldAndMessageExceptionInfoDTO(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
