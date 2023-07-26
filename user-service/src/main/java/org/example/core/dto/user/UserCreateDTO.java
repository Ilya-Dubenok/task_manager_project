package org.example.core.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;

public class UserCreateDTO {

    @Email(message = "должен быть валидным адресом")
    @NotBlank(message = "должен быть валидным адресом")
    private String mail;

    @NotBlank(message = "не должен быть пустым")
    private String fio;

    @NotNull(message = "не задана роль")
    private UserRole role;

    @NotNull(message = "не задан статус")
    private UserStatus status;

    @Size(min = 5, message = "должен быть не меньше пяти символов")
    @NotNull(message = "должен быть не меньше пяти символов")
    private String password;

    public UserCreateDTO() {
    }

    public UserCreateDTO(String mail, String fio, UserRole role, UserStatus status, String password) {
        this.mail = mail;
        this.fio = fio;
        this.role = role;
        this.status = status;
        this.password = password;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
