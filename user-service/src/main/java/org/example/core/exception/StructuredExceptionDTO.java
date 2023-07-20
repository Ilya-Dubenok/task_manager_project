package org.example.core.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructuredExceptionDTO {

    private String logref = "structured_error";

    private List<SingleFieldAndMessageExceptionInfoDTO> errors;

    public StructuredExceptionDTO() {
    }

    public StructuredExceptionDTO(StructuredException exception) {
        errors = new ArrayList<>();

        Map<String, String> exceptionsMap = exception.getExceptionsMap();
        for (Map.Entry<String, String> entry : exceptionsMap.entrySet()) {
            errors.add(
                    new SingleFieldAndMessageExceptionInfoDTO(
                            entry.getKey(),
                            entry.getValue()
                    )
            );

        }
    }

    public String getLogref() {
        return logref;
    }

    public void setLogref(String logref) {
        this.logref = logref;
    }

    public List<SingleFieldAndMessageExceptionInfoDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<SingleFieldAndMessageExceptionInfoDTO> errors) {
        this.errors = errors;
    }
}
