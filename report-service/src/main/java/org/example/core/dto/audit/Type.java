package org.example.core.dto.audit;

public enum Type {
    USER("user"), TASK("task"), PROJECT("project");

    private String id;

    Type(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
