package edu.cit.abel.washq.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;
    private String timestamp;

    // --- Static factory methods ---

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.error = null;
        response.timestamp = LocalDateTime.now().toString();
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.data = null;
        response.error = new ErrorDetail(code, message, details);
        response.timestamp = LocalDateTime.now().toString();
        return response;
    }

    // --- Getters and Setters ---

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // --- Inner class for error details ---

    public static class ErrorDetail {
        private String code;
        private String message;
        private Object details;

        public ErrorDetail() {}

        public ErrorDetail(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }
    }
}
