package edu.cit.abel.washq.exception;

public class BookingException extends RuntimeException {

    private final String errorCode;

    public BookingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
