package org.example.endpoint.web.exception_handler;

import com.google.common.base.CaseFormat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.example.core.exception.GeneralException;
import org.example.core.exception.GeneralExceptionDTO;
import org.example.core.exception.StructuredException;
import org.example.core.exception.StructuredExceptionDTO;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final String MESSAGE_FOR_UNDEFINED_EXCEPTION =
            "Внутренняя ошибка сервера. Сервер не смог корректно обработать запрос";

    private static final String MESSAGE_FOR_INVALID_INPUT_DATA = "Запрос содержит некорректные данные " +
            "или необходимые данные отсутствуют. Измените запрос и отправьте его снова";


    @ExceptionHandler(value = StructuredException.class)
    protected ResponseEntity<Object> handleStructuredException(StructuredException e, WebRequest request) {
        StructuredExceptionDTO structuredExceptionDTO = new StructuredExceptionDTO(e);
        return new ResponseEntity<>(structuredExceptionDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GeneralException.class)
    protected ResponseEntity<Object> handleGeneralException(GeneralException e, WebRequest request) {
        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO();
        generalExceptionDTO.setMessage(e.getMessage());
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, WebRequest request) {


        StructuredExceptionDTO structuredExceptionDTO = parseConstraintViolationException(e);

        return new ResponseEntity<>(structuredExceptionDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleUndefinedException(Exception e, WebRequest request) {
        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(
                new GeneralException(MESSAGE_FOR_UNDEFINED_EXCEPTION, e)
        );
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e, WebRequest request) {

        StructuredException structuredException = new StructuredException();

        if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
            StructuredExceptionDTO structuredExceptionDTO = new StructuredExceptionDTO(structuredException);
            return new ResponseEntity<>(structuredExceptionDTO, HttpStatus.BAD_REQUEST);
        }

        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(
                new GeneralException(MESSAGE_FOR_UNDEFINED_EXCEPTION, e)
        );
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        status = HttpStatus.BAD_REQUEST;
        String propertyName = ex.getPropertyName();
        StructuredException structuredException = new StructuredException();
        if (Objects.equals(propertyName, "uuid") ||
                Objects.equals(propertyName, "page") ||
                Objects.equals(propertyName, "size")
        ) {
            structuredException.put(
                    propertyName, String.format(MESSAGE_FOR_INVALID_PROPERTY, propertyName)
            );
            return new ResponseEntity<>(
                    new StructuredExceptionDTO(structuredException), status
            );

        }

        return new ResponseEntity<>(List.of(new GeneralExceptionDTO(MESSAGE_FOR_INVALID_INPUT_DATA)),
                status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        GeneralExceptionDTO generalExceptionDTO = new GeneralExceptionDTO(MESSAGE_FOR_INVALID_INPUT_DATA);
        return new ResponseEntity<>(List.of(generalExceptionDTO), HttpStatus.BAD_REQUEST);

    }

    private StructuredExceptionDTO parseConstraintViolationException(ConstraintViolationException e) {
        StructuredException structuredException = new StructuredException();
        Iterator<ConstraintViolation<?>> iterator = e.getConstraintViolations().iterator();
        while (iterator.hasNext()) {
            ConstraintViolation<?> constraintViolation = iterator.next();
            String propName = parseForPropNameInSnakeCase(constraintViolation);
            String message = constraintViolation.getMessage();
            structuredException.put(propName, message);


        }


        return new StructuredExceptionDTO(structuredException);
    }

    private String parseForPropNameInSnakeCase(ConstraintViolation<?> next) {

        Path propertyPath = next.getPropertyPath();


        Iterator<Path.Node> iterator = propertyPath.iterator();
        Path.Node node = null;

        while (iterator.hasNext()) {
            node = iterator.next();

        }

        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, node.getName());


    }


}
