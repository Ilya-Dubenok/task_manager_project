package org.example.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.example.core.dto.validation.Uidable;

import java.util.Objects;
import java.util.UUID;

public class UserDTO implements Uidable {

    @NotNull(message = "не задан uuid пользователя")
    private UUID uuid;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UserRole role;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String mail;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String fio;


    public UserDTO() {
    }

    public UserDTO(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(uuid, userDTO.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
