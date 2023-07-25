package org.example.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {


    @Email(message = "должен быть валидным адресом")
    @NotBlank(message = "должен быть валидным адресом")
    private String mail;

    @NotBlank(message = "не должен быть пустым")
    private String fio;

    @Size(min = 5, message = "должен быть не меньше пяти символов")
    @NotNull(message = "должен быть не меньше пяти символов")
    private String password;


    public UserRegistrationDTO() {
    }

    public UserRegistrationDTO(String mail, String fio, String password) {
        this.mail = mail;
        this.fio = fio;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
