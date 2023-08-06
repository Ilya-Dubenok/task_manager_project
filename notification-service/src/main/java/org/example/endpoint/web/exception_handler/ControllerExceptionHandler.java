package org.example.endpoint.web.exception_handler;

import org.example.core.exception.GeneralException;
import org.example.core.exception.GeneralExceptionDTO;
import org.example.core.exception.StructuredException;
import org.example.core.exception.StructuredExceptionDTO;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_FOR_UNDEFINED_EXCEPTION = "Внутренняя ошибка сервера. Сервер не смог корректно обработать запрос";

    private static final String MESSAGE_FOR_INVALID_INPUT_DATA = "Запрос содержит некорректные данные " +
            "или необходимые данные отсутствуют. Измените запрос и отправьте его снова";

    @ExceptionHandler(value = StructuredException.class)
    protected ResponseEntity<Object> handleStructuredException(StructuredException e, WebRequest request) {
        StructuredExceptionDTO dto = new StructuredExceptionDTO(e);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GeneralException.class)
    protected ResponseEntity<Object> handleGeneralException(GeneralException e, WebRequest request) {
        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO();
        generalExceptionDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleUndefinedException(Exception e, WebRequest request) {

        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(
                new GeneralException(MESSAGE_FOR_UNDEFINED_EXCEPTION, e)
        );
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(List.of(new GeneralExceptionDTO(MESSAGE_FOR_INVALID_INPUT_DATA)),
                status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(MESSAGE_FOR_INVALID_INPUT_DATA);
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.BAD_REQUEST);

    }






}
