package org.example.core.exception;

public class GeneralExceptionDTO {

    private final String logref = "error";

    private String message;

    public GeneralExceptionDTO() {
    }

    public GeneralExceptionDTO(String message) {
        this.message = message;
    }

    public GeneralExceptionDTO(GeneralException exception) {
        this.message = exception.getMessage();
    }

    public String getLogref() {
        return logref;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
