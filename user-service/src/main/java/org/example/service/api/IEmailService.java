package org.example.service.api;

import java.util.UUID;

public interface IEmailService {



    void sendMessage(String to, String text, String subject);


    void sendVerificationCodeMessage(String mail, Integer verificationCode);
}
