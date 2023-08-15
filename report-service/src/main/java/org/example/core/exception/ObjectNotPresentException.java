package org.example.core.exception;

public class ObjectNotPresentException extends RuntimeException {


    public ObjectNotPresentException(String message) {
        super(message);
    }

    public ObjectNotPresentException(Throwable cause) {
        super(cause);
    }
}
