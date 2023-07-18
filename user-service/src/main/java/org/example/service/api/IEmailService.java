package org.example.service.api;

public interface IEmailService {



    void sendMessage(String to, String text, String subject);


    void sendVerificationCodeMessage(String mail, Integer verificationCode);
}
