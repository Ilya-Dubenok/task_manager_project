package org.example.core.exception;

public class GeneralException extends RuntimeException{



    public static final String DEFAULT_SEND_VERIFICATION_EMAIL_EXCEPTION = "Произошла ошибка при отправке кода подтверждения " +
            "по электронной почте. Проверьте правильность введенной электронной почты или попробуйте позже";


    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
