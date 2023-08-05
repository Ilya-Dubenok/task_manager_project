package org.example.core.dto.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.dao.entities.audit.Type;
import org.example.core.dto.user.UserAuditDTO;

public class AuditCreateDTO {

    @NotNull(message = "поле пользователя не должно быть null")
    private UserAuditDTO user;

    @NotBlank(message = "текст не должен быть пустым")
    private String text;

    @NotNull(message = "type не должен быть пустым")
    private Type type;

    @NotBlank(message = "id не должен быть пустым")
    private String id;

    public AuditCreateDTO() {
    }

    public AuditCreateDTO(UserAuditDTO user, String text, Type type, String id) {
        this.user = user;
        this.text = text;
        this.type = type;
        this.id = id;
    }

    public UserAuditDTO getUser() {
        return user;
    }

    public void setUser(UserAuditDTO user) {
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
