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

    @ExceptionHandler(value = StructuredException.class)
    protected ResponseEntity<Object> handleConflict(StructuredException e, WebRequest request) {
        //TODO TEST
        StructuredExceptionDTO dto = new StructuredExceptionDTO(e);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GeneralException.class)
    protected ResponseEntity<Object> handleConflict(GeneralException e, WebRequest request) {
        GeneralExceptionDTO dto = new GeneralExceptionDTO();
        dto.setMessage(e.getMessage());
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        //TODO ADD ALL NECESSARY CHECKS
        status = HttpStatus.BAD_REQUEST;
        String propertyName = ex.getPropertyName();
        return new ResponseEntity<>(propertyName, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        GeneralExceptionDTO dto = new GeneralExceptionDTO("Запрос содержит некорректные данные " +
                "или необходимые данные отсутствуют. Измените запрос и отправьте его снова");
        return new ResponseEntity<>(List.of(dto), HttpStatus.BAD_REQUEST);

    }

    //TODO add GenerealException status 500
}
