package edu.cit.abel.washq.exception;

public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;

    public DuplicateResourceException(String message) {
        super(message);
        this.errorCode = "DB-002";
    }

    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
