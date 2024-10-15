package com.intuit.be_a_friend.validators;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.exceptions.InsufficientInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserSignupDataValidator implements Validator<UserDTO> {

    private static final Logger logger = LoggerFactory.getLogger(UserSignupDataValidator.class);

    @Override
    public boolean validate(UserDTO input) throws Exception {
        logger.info("Starting validation for user: {}", input.getUsername());

        if (input.getUsername() == null || input.getUsername().isEmpty()) {
            logger.error("Validation failed: Username cannot be empty");
            throw new InsufficientInformationException("Username cannot be empty");
        }
        if (input.getPassword() == null || input.getPassword().isEmpty()) {
            logger.error("Validation failed: Password cannot be empty");
            throw new InsufficientInformationException("Password cannot be empty");
        }
        if (input.getEmail() == null || input.getEmail().isEmpty()) {
            logger.error("Validation failed: Email cannot be empty");
            throw new InsufficientInformationException("Email cannot be empty");
        }
        if (input.getPhoneNumber() != null && input.getPhoneNumber().matches("\\d{10}")) {
            logger.error("Validation failed: Phone number should be of length 10 and contain only numbers");
            throw new IllegalArgumentException("Phone number should be of length 10");
        }

        logger.info("Validation successful for user: {}", input.getUsername());
        return true;
    }
}