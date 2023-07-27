package org.example.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.dao.entities.audit.Type;
import org.example.dao.entities.user.User;

public class AuditCreateDTO {

    @NotNull(message = "поле пользователя не должно быть null")
    private User user;

    @NotBlank(message = "текст не должен быть пустым")
    private String text;

    @NotNull(message = "type не должен быть пустым")
    private Type type;

    @NotBlank(message = "id не должен быть пустым")
    private String id;

    public AuditCreateDTO() {
    }

    public AuditCreateDTO(User user, String text, Type type, String id) {
        this.user = user;
        this.text = text;
        this.type = type;
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
