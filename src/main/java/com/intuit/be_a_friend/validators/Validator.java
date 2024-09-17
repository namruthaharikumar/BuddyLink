package com.intuit.be_a_friend.validators;


public interface Validator<T> {
    boolean validate(T input) throws Exception;
}
