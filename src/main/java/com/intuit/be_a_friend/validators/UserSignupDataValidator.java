package com.intuit.be_a_friend.validators;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.exceptions.InsufficientInformationException;
import org.springframework.stereotype.Component;


@Component
public class UserSignupDataValidator implements Validator<UserDTO> {

    @Override
    public boolean validate(UserDTO input) throws Exception {
        if (input.getUsername() == null || input.getUsername().isEmpty()) {
            throw new InsufficientInformationException("Username cannot be empty");
        }
        if (input.getPassword() == null || input.getPassword().isEmpty()) {
            throw new InsufficientInformationException("Password cannot be empty");
        }
        if (input.getEmail() == null || input.getEmail().isEmpty()) {
            throw new InsufficientInformationException("Email cannot be empty");
        }
        if (input.getAccountType() == null) {
            throw new InsufficientInformationException("Account type cannot be empty");
        }
        return true;
    }
}
