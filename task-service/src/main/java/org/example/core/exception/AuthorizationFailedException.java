package org.example.core.exception;

public class AuthorizationFailedException extends RuntimeException{

    public AuthorizationFailedException(String message) {
        super(message);
    }
}
