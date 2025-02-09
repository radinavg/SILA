package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestData;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.endpoint.dto.user.UserLoginDto;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LoginEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataGenerator testDataGenerator;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        String email = "successful.user@email.com";
        String password = "GoodPassword123";

        UserLoginDto userLoginDto = new UserLoginDto(email, password);

        String userLoginDtoJson = objectMapper.writeValueAsString(userLoginDto);

        MvcResult mvcResult = mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userLoginDtoJson))
                .andExpect(status().isOk())
                .andReturn();

        String tokenResponse = mvcResult.getResponse().getContentAsString();
        String tokenPrefix = "Bearer ";

        if (tokenResponse.startsWith(tokenPrefix)) {
            String token = tokenResponse.substring(tokenPrefix.length());
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }
    }

    @Test
    void testLoginReturns403StatusDueToBlocked() throws Exception {
        String email = "forbidden.user@email.com";
        String password = "GoodPassword123";

        UserLoginDto userLoginDto = new UserLoginDto(email, password);

        String userLoginDtoJson = objectMapper.writeValueAsString(userLoginDto);

        MvcResult mvcResult = mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userLoginDtoJson))
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("User account is locked"))
        );
    }

    @Test
    void testLoginReturns401StatusDueToWrongPassword() throws Exception {
        String email = "successful.user@email.com";
        String password = "BadPassword";

        UserLoginDto userLoginDto = new UserLoginDto(email, password);

        String userLoginDtoJson = objectMapper.writeValueAsString(userLoginDto);

        MvcResult mvcResult = mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userLoginDtoJson))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("Username or password is incorrect"))
        );
    }

    @Test
    void testBlockUserAfterFifthFailedLoginAttempt() throws Exception {
        UserLoginDto userLoginDto = new UserLoginDto(
                "successful.user@email.com",
                "BadPassword123"
        );

        this.mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        this.mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        this.mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        this.mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();


        MvcResult mvcResult = this.mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDto)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        ApplicationUser user = applicationUserRepository.findByEmail(userLoginDto.email());

        assertAll(
                () -> assertEquals(mvcResult.getResponse().getStatus(), HttpStatus.FORBIDDEN.value()),
                () -> assertTrue(user.isLocked())
        );
    }

    @Test
    void testLoginReturns401WhenTryingToLoginUnapprovedStudioAdmin() throws Exception {
        String email = "studio1admin@email.com";
        String password = "password";

        UserLoginDto userLoginDto = new UserLoginDto(email, password);

        String userLoginDtoJson = objectMapper.writeValueAsString(userLoginDto);

        MvcResult mvcResult = mockMvc.perform(post(AUTHENTICATION_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userLoginDtoJson))
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("Username or password is incorrect"))
        );
    }

}
