package com.inso.sila.unittest;

import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.service.StudioService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class StudioServiceTest {

    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private StudioService studioService;

    @BeforeEach
    void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
        MockitoAnnotations.openMocks(this);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("ticket.admin@gmail.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
