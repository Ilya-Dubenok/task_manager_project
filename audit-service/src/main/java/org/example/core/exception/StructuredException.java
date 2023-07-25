package org.example.core.exception;

import java.util.HashMap;
import java.util.Map;

public class StructuredException extends RuntimeException {

    private Map<String, String> exceptionsMap = new HashMap<>();

    public StructuredException() {
    }

    public StructuredException(String paramName, String message) {
        this.exceptionsMap.put(paramName, message);
    }

    public Map<String, String> getExceptionsMap() {
        return exceptionsMap;
    }

    public void setExceptionsMap(Map<String, String> exceptionsMap) {
        this.exceptionsMap = exceptionsMap;
    }

    public int getSize() {
        return exceptionsMap.size();
    }

    public boolean hasExceptions() {
        return exceptionsMap.size() > 0;
    }

    public void put(String paramName, String message) {
        exceptionsMap.put(paramName, message);
    }

}
