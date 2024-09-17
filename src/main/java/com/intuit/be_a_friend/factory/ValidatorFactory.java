package com.intuit.be_a_friend.factory;

import com.intuit.be_a_friend.enums.OPERATION;
import com.intuit.be_a_friend.validators.UserSignupDataValidator;
import com.intuit.be_a_friend.validators.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidatorFactory {
    @Autowired
    private UserSignupDataValidator userSignupDataValidator;

    public Validator getValidator(OPERATION type) {
       switch (type) {
           case SIGNUP:
               return userSignupDataValidator;
           default:
               return null;
       }
    }
}
