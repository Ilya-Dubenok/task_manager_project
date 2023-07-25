package org.example.core.dto.audit;


public class AuditCreateDTO {

    private AuditUserDTO user;

    private String text;

    private Type type;

    public AuditCreateDTO() {
    }

    public AuditCreateDTO(AuditUserDTO user, String text, Type type) {
        this.user = user;
        this.text = text;
        this.type = type;
    }

    public AuditUserDTO getUser() {
        return user;
    }

    public void setUser(AuditUserDTO user) {
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
}
