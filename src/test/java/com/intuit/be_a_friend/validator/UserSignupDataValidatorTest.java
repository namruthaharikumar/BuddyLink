package com.intuit.be_a_friend.validator;

import com.intuit.be_a_friend.DTO.UserDTO;
import com.intuit.be_a_friend.enums.AccountType;
import com.intuit.be_a_friend.exceptions.InsufficientInformationException;
import com.intuit.be_a_friend.validators.UserSignupDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSignupDataValidatorTest {

    private UserSignupDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserSignupDataValidator();
    }

    @Test
    void testValidate_ValidInput() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setEmail("testuser@example.com");
        userDTO.setAccountType(AccountType.PRIVATE);

        assertTrue(validator.validate(userDTO));
    }

    @Test
    void testValidate_MissingUsername() {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password");
        userDTO.setEmail("testuser@example.com");
        userDTO.setAccountType(AccountType.PRIVATE);

        Exception exception = assertThrows(InsufficientInformationException.class, () -> {
            validator.validate(userDTO);
        });

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    void testValidate_MissingPassword() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("testuser@example.com");
        userDTO.setAccountType(AccountType.PRIVATE);

        Exception exception = assertThrows(InsufficientInformationException.class, () -> {
            validator.validate(userDTO);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void testValidate_MissingEmail() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setAccountType(AccountType.PRIVATE);

        Exception exception = assertThrows(InsufficientInformationException.class, () -> {
            validator.validate(userDTO);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void testValidate_MissingAccountType() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setEmail("testuser@example.com");

        Exception exception = assertThrows(InsufficientInformationException.class, () -> {
            validator.validate(userDTO);
        });

        assertEquals("Account type cannot be empty", exception.getMessage());
    }
}