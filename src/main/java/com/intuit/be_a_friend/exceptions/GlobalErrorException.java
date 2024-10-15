package com.intuit.be_a_friend.exceptions;

public class GlobalErrorException extends RuntimeException {
    private final int statusCode;
    private final String message;

    public GlobalErrorException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
