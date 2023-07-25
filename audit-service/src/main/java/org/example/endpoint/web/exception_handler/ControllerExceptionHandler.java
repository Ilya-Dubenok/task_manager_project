package org.example.endpoint.web.exception_handler;

import com.google.common.base.CaseFormat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_FOR_INVALID_PROPERTY =
            "%s не может быть распознан";

    @ExceptionHandler(value = StructuredException.class)
    protected ResponseEntity<Object> handleConflict(StructuredException e, WebRequest request) {
        StructuredExceptionDTO dto = new StructuredExceptionDTO(e);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GeneralException.class)
    protected ResponseEntity<Object> handleConflict(GeneralException e, WebRequest request) {
        GeneralExceptionDTO dto = new GeneralExceptionDTO();
        dto.setMessage(e.getMessage());
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConflict(ConstraintViolationException e, WebRequest request) {


       StructuredExceptionDTO structuredExceptionDTO = parseConstraintViolationException(e);

        return new ResponseEntity<>(structuredExceptionDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(
                new GeneralException("Внутренняя ошибка сервера. Сервер не смог корректно обработать запрос", e)
        );
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        //TODO ADD ALL NECESSARY CHECKS
        status = HttpStatus.BAD_REQUEST;
        String propertyName = ex.getPropertyName();
        StructuredException structuredException = new StructuredException();
        if (Objects.equals(propertyName, "uuid") ||
                Objects.equals(propertyName, "page") ||
                Objects.equals(propertyName, "size") ||
                Objects.equals(propertyName, "dt_update")
        ) {
            structuredException.put(
                    propertyName, String.format(MESSAGE_FOR_INVALID_PROPERTY, propertyName)
            );
            return new ResponseEntity<>(
                    new StructuredExceptionDTO(structuredException), status
            );

        }

        return new ResponseEntity<>(List.of(new GeneralExceptionDTO("При обработке входных параметров произошла ошибка")),
                status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        GeneralExceptionDTO dto = new GeneralExceptionDTO("Запрос содержит некорректные данные " +
                "или необходимые данные отсутствуют. Измените запрос и отправьте его снова");
        return new ResponseEntity<>(List.of(dto), HttpStatus.BAD_REQUEST);

    }

    private StructuredExceptionDTO parseConstraintViolationException(ConstraintViolationException e) {
        StructuredException exception = new StructuredException();
        Iterator<ConstraintViolation<?>> iterator = e.getConstraintViolations().iterator();
        while (iterator.hasNext()) {
            ConstraintViolation<?> constraintViolation = iterator.next();
            String propName = parseForPropNameInSnakeCase(constraintViolation);
            String message = constraintViolation.getMessage();
            exception.put(propName, message);


        }


        return new StructuredExceptionDTO(exception);
    }

    private String parseForPropNameInSnakeCase(ConstraintViolation<?> next) {

        Path propertyPath = next.getPropertyPath();


        Iterator<Path.Node> iterator = propertyPath.iterator();
        Path.Node node = null;

        while (iterator.hasNext()) {
            node = iterator.next();

        }

        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,node.getName());


    }


}
