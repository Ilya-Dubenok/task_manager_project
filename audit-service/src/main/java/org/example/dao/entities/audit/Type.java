package org.example.dao.entities.audit;

public enum Type {
    USER("user");

    private String id;

    Type(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
