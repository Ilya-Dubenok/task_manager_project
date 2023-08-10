package org.example.core.exception;

public class AuthenticationFailedException extends RuntimeException{

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
