package org.example.core.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UserLoginDTO {

    @NotBlank
    private String mail;

    @NotBlank
    private String password;

    public UserLoginDTO(String mail, String password) {
        this.mail = mail;
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
