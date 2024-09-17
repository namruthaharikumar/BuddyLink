package com.intuit.be_a_friend.exceptions;

public class DuplicateUserInformationException extends RuntimeException {
    public DuplicateUserInformationException(String message) {
        super(message);
    }
}
