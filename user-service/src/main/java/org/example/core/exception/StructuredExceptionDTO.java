package org.example.core.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuredExceptionDTO {

    private String logref = "structured_error";

    private List<Map<String, String>> errors;

    public StructuredExceptionDTO() {
    }

    public StructuredExceptionDTO(StructuredException exception) {
        errors = new ArrayList<>();

        Map<String, String> exceptionsMap = exception.getExceptionsMap();
        for (Map.Entry<String, String> entry : exceptionsMap.entrySet()) {
            errors.add(
                    Map.of(
                            "field", entry.getKey()
                    )
            );
            errors.add(
                    Map.of(
                            "message", entry.getValue()
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

    public List<Map<String, String>> getErrors() {
        return errors;
    }

    public void setErrors(List<Map<String, String>> errors) {
        this.errors = errors;
    }
}
