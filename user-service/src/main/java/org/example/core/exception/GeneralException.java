package org.example.core.exception;

public class GeneralException extends RuntimeException{



    public static final String DEFAULT_DATABASE_EXCEPTION_MESSAGE="Произошла ошибка при обработке запроса. Измените запрос" +
            " и попробуйте снова";

    public static final String DEFAULT_SEND_VERIFICATION_EMAIL_EXCEPTION = "Произошла ошибка при отправке кода подтверждения " +
            "по электронной почте. Проверьте правильной введенной электронной почты или попробуйте позже";


    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
