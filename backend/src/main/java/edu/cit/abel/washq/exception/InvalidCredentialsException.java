package edu.cit.abel.washq.exception;

public class InvalidCredentialsException extends RuntimeException {

    private final String errorCode;

    public InvalidCredentialsException() {
        super("Email or password is incorrect");
        this.errorCode = "AUTH-001";
    }

    public InvalidCredentialsException(String message) {
        super(message);
        this.errorCode = "AUTH-001";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
