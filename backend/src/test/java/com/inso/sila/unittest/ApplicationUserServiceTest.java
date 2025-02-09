package com.inso.sila.unittest;

import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.endpoint.dto.user.UserEmailDto;
import com.inso.sila.endpoint.dto.user.UserRegisterDto;
import com.inso.sila.enums.Gender;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.ValidationException;
import com.inso.sila.service.ApplicationUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class ApplicationUserServiceTest {

    @Autowired
    private ApplicationUserService userService;
    @Autowired
    private TestDataGenerator testDataGenerator;

    @BeforeEach
    void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
        MockitoAnnotations.openMocks(this);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("ticket.admin@gmail.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createUserSuccessfully() throws ConflictException, ValidationException {
        UserRegisterDto user = new UserRegisterDto(
                "maja@gmail.com",
                "HelloWorld1!23",
                "HelloWorld1!23",
                "Marijana",
                "Petojevic",
                Gender.FEMALE,
                "My Address 123",
                0,
                0
        );
        UserEmailDto email = userService.registerUser(user);
        assertAll(
                () -> assertNotNull(email),
                () -> assertEquals("maja@gmail.com", email.email())
        );
    }
}
