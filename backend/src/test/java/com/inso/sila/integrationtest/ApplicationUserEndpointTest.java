package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestData;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.ApplicationUserEndpoint;
import com.inso.sila.endpoint.dto.user.UpdateUserInfoDto;
import com.inso.sila.endpoint.dto.user.UserDetailDto;
import com.inso.sila.endpoint.dto.user.UserEmailDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.dto.user.UserPreferencesDto;
import com.inso.sila.endpoint.dto.user.UserRegisterDto;
import com.inso.sila.endpoint.dto.user.UserUpdatePasswordDto;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.Studio;
import com.inso.sila.enums.Gender;
import com.inso.sila.repository.ApplicationUserPreferencesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApplicationUserEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationUserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    private StudioRepository studioRepository;
    @Autowired
    private ApplicationUserEndpoint applicationUserEndpoint;
    @Autowired
    private ApplicationUserPreferencesRepository applicationUserPreferencesRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void createUserSuccessfully() throws Exception {
        UserRegisterDto userCreateDto = new UserRegisterDto(
                "john.doe@example.com",
                "Password1!7865",
                "Password1!7865",
                "John",
                "Doe",
                Gender.MALE,
                "My Street 1",
                0,
                0
        );

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserEmailDto createdUser = objectMapper.readValue(response.getContentAsString(), UserEmailDto.class);

        assertAll(
                () -> assertNotNull(createdUser),
                () -> assertEquals("john.doe@example.com", createdUser.email())
        );
    }

    @Test
    void createUserFailsWithConflictException() throws Exception {
        ApplicationUser existingUser = ApplicationUser.builder()
                .firstName("Existing")
                .lastName("User")
                .email("existing.email@example.com")
                .password(passwordEncoder.encode("Password1!"))
                .location("123 Existing St")
                .gender(Gender.FEMALE)
                .isAdmin(false)
                .isLocked(false)
                .build();
        userRepository.save(existingUser);

        UserRegisterDto userCreateDto = new UserRegisterDto(
                "existing.email@example.com",
                "Password1!7865",
                "Password1!7865",
                "John",
                "Doe",
                Gender.MALE,
                "My Street 1",
                0,
                0
        );

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("Couldn't create user"))
        );
    }

    @Test
    void createUserFailsWithValidationExceptionDueToUnacceptablePassword() throws Exception {
        UserRegisterDto userCreateDto = new UserRegisterDto(
                "email@example.com",
                "hello",
                "hello",
                "John",
                "Doe",
                Gender.MALE,
                "My Street 1",
                0,
                0
        );

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");
        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("Validation failed"))
        );
    }

    @Test
    void createUserFailsWithValidationExceptionDueToPasswordConfirmation() throws Exception {
        UserRegisterDto userCreateDto = new UserRegisterDto(
                "email@example.com",
                "heLlo1?4",
                "heLlo1?3",
                "John",
                "Doe",
                Gender.MALE,
                "My Street 1",
                0,
                0
        );

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");
        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.asText().contains("Validation failed"))
        );
    }

    @Test
    void testAuthorizedResetPasswordRequestAsUser() throws Exception{
        UserEmailDto resetUserPasswordDto = new UserEmailDto("successful.user@email.com");
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/reset/password")
                        .content(objectMapper.writeValueAsString(resetUserPasswordDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserEmailDto resetUserPasswordDto1 = objectMapper.readValue(response.getContentAsString(), UserEmailDto.class);

        assertAll(
                () -> assertEquals(mvcResult.getResponse().getStatus(), HttpStatus.OK.value()),
                () -> assertNotNull(resetUserPasswordDto1)
        );
    }

    @Test
    void testUnblockBlockedUserWhileResettingPassword() throws Exception {
        UserEmailDto resetUserPasswordDto = new UserEmailDto("blocked.user@email.com");
        String oldPswd = applicationUserRepository.findByEmail(resetUserPasswordDto.email()).getPassword();
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/reset/password")
                        .content(objectMapper.writeValueAsString(resetUserPasswordDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserEmailDto resetUserPasswordDto1 = objectMapper.readValue(response.getContentAsString(), UserEmailDto.class);
        ApplicationUser user = applicationUserRepository.findByEmail(resetUserPasswordDto1.email());

        assertAll(
                () -> assertEquals(mvcResult.getResponse().getStatus(), HttpStatus.OK.value()),
                () -> assertNotNull(resetUserPasswordDto1),
                () -> assertFalse(user.isLocked()),
                () -> assertFalse(passwordEncoder.matches(oldPswd, user.getPassword()))

        );
    }

    @Test
    void testInvalidResetPasswordRequestAsUser() throws Exception{
        UserEmailDto resetUserPasswordDto = new UserEmailDto("harper.hernandez");
        this.mockMvc.perform(put(APPLICATION_USER_BASE + "/reset/password")
                        .content(objectMapper.writeValueAsString(resetUserPasswordDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUnknownResetPasswordRequestAsUser() throws Exception{
        UserEmailDto resetUserPasswordDto = new UserEmailDto("my.random@email.ac.at");
        this.mockMvc.perform(put(APPLICATION_USER_BASE + "/reset/password")
                        .content(objectMapper.writeValueAsString(resetUserPasswordDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testSuccessfulPasswordUpdateAsUser() throws Exception {
        UserUpdatePasswordDto userUpdatePasswordDto = new UserUpdatePasswordDto("password", "paSSword12!", "paSSword12!");
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update/password")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDto)))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserEmailDto responseEntity = objectMapper.readValue(response.getContentAsString(), UserEmailDto.class);
        ApplicationUser user = applicationUserRepository.findByEmail(responseEntity.email());

        assertAll(
                () -> assertEquals(mvcResult.getResponse().getStatus(), HttpStatus.OK.value()),
                () -> assertNotNull(responseEntity),
                () -> assertEquals("admin@email.com", responseEntity.email()),
                () -> assertTrue(passwordEncoder.matches("paSSword12!", user.getPassword()))
        );

    }

    @Test
    void testInvalidUpdateUserPasswordAsUserThrowsValidationDueInvalidPasswordFormat() throws Exception {
        UserUpdatePasswordDto userUpdatePasswordDto = new UserUpdatePasswordDto("password", "password", "password");

        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update/password")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(3, errorsNode.size(), "Errors array should contain 3 items"),
                () -> assertTrue(errorsNode.toString().contains("Password must contain at least one uppercase letter"), "Missing error for uppercase letter"),
                () -> assertTrue(errorsNode.toString().contains("Password must contain at least one number"), "Missing error for number"),
                () -> assertTrue(errorsNode.toString().contains("Password must contain at least one special character"), "Missing error for special character")
        );
    }

    @Test
    void testUpdatePasswordFailsDueToWrongCurrentPasswordValidationException() throws Exception {
        UserUpdatePasswordDto userUpdatePasswordDto = new UserUpdatePasswordDto("password123", "password", "password");

        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update/password")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("Current password does not match expected one"))
        );
    }

    @Test
    void testUpdatePasswordFailsDueToNewPasswordAndConfirmationPasswordBeingNotTheSame() throws Exception {
        UserUpdatePasswordDto userUpdatePasswordDto = new UserUpdatePasswordDto("password", "passWord!1", "passWord!12");

        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update/password")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDto)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("Passwords do not match"))
        );

    }


    @Test
    void testUserSetsPreferencesCorrectly() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(applicationUserEndpoint, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        UserPreferencesDto userPreferencesDto = new UserPreferencesDto(
                true,
                true,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                false,
                true,
                true,
                10,
                true,
                true,
                true,
                true,
                true
        );

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create-preferences")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPreferencesDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserInfoDto user = objectMapper.readValue(response.getContentAsString(), UserInfoDto.class);
        ApplicationUser userEntity = applicationUserRepository.findByEmail(user.email());
        ApplicationUserPreferences userPref = applicationUserPreferencesRepository.findById(userEntity.getApplicationUserId());
        assertAll(
                () -> assertNotNull(user),
                () -> assertNotNull(userEntity),
                () -> assertNotNull(userPref),
                () -> assertEquals(userPreferencesDto.prefersIndividual(), userPref.isPrefersIndividual()),
                () -> assertEquals(userPreferencesDto.prefersTeam(), userPref.isPrefersTeam()),
                () -> assertEquals(userPreferencesDto.prefersWaterBased(), userPref.isPrefersWaterBased()),
                () -> assertEquals(userPreferencesDto.prefersIndoor(), userPref.isPrefersIndoor()),
                () -> assertEquals(userPreferencesDto.prefersOutdoor(), userPref.isPrefersOutdoor()),
                () -> assertEquals(userPreferencesDto.prefersBothIndoorAndOutdoor(), userPref.isPrefersBothIndoorAndOutdoor()),
                () -> assertEquals(userPreferencesDto.prefersWarmClimate(), userPref.isPrefersWarmClimate()),
                () -> assertEquals(userPreferencesDto.prefersColdClimate(), userPref.isPrefersColdClimate()),
                () -> assertEquals(userPreferencesDto.rainCompatibility(), userPref.isRainCompatibility()),
                () -> assertEquals(userPreferencesDto.windSuitability(), userPref.isWindSuitability()),
                () -> assertEquals(userPreferencesDto.focusUpperBody(), userPref.isFocusUpperBody()),
                () -> assertEquals(userPreferencesDto.focusLowerBody(), userPref.isFocusLowerBody()),
                () -> assertEquals(userPreferencesDto.focusCore(), userPref.isFocusCore()),
                () -> assertEquals(userPreferencesDto.focusFullBody(), userPref.isFocusFullBody()),
                () -> assertEquals(userPreferencesDto.isBeginner(), userPref.isBeginner()),
                () -> assertEquals(userPreferencesDto.isIntermediate(), userPref.isIntermediate()),
                () -> assertEquals(userPreferencesDto.isAdvanced(), userPref.isAdvanced()),
                () -> assertEquals(userPreferencesDto.physicalDemandLevel(), userPref.getPhysicalDemandLevel()),
                () -> assertEquals(userPreferencesDto.goalStrength(), userPref.isGoalStrength()),
                () -> assertEquals(userPreferencesDto.goalEndurance(), userPref.isGoalEndurance()),
                () -> assertEquals(userPreferencesDto.goalFlexibility(), userPref.isGoalFlexibility()),
                () -> assertEquals(userPreferencesDto.goalBalanceCoordination(), userPref.isGoalBalanceCoordination()),
                () -> assertEquals(userPreferencesDto.goalMentalFocus(), userPref.isGoalMentalFocus())

        );
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/on-preference", recordedRequest.getPath());
        mockWebServer.shutdown();
    }


    @Test
    void testSettingUSerPreferencesFailsWithConflictExceptionBecauseForTheUserPreferencesHaveBeenAlreadySet() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(applicationUserEndpoint, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        UserPreferencesDto userPreferencesDto = new UserPreferencesDto(
                true,
                true,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                false,
                true,
                true,
                10,
                true,
                true,
                true,
                true,
                true
        );

        this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create-preferences")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPreferencesDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult mvcResult = this.mockMvc.perform(post(APPLICATION_USER_BASE + "/create-preferences")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPreferencesDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorMessage = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorMessage),
                () -> assertTrue(errorMessage.toString().contains("Preferences already exist for user with ID"))
        );
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/on-preference", recordedRequest.getPath());
        mockWebServer.shutdown();
    }

    @Test
    void testUpdateUserPreferencesSuccessfully() throws Exception {
        ApplicationUser user = applicationUserRepository.findByEmail("user.preferences@email.com");
        ApplicationUserPreferences userPref = applicationUserPreferencesRepository.findById(user.getApplicationUserId());
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(applicationUserEndpoint, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        UserPreferencesDto newUserPreferences = new UserPreferencesDto(
                userPref.isPrefersIndividual(),
                userPref.isPrefersTeam(),
                userPref.isPrefersWaterBased(),
                userPref.isPrefersIndoor(),
                userPref.isPrefersOutdoor(),
                userPref.isPrefersBothIndoorAndOutdoor(),
                userPref.isPrefersWarmClimate(),
                userPref.isPrefersColdClimate(),
                userPref.isRainCompatibility(),
                userPref.isWindSuitability(),
                userPref.isFocusUpperBody(),
                userPref.isFocusLowerBody(),
                userPref.isFocusCore(),
                userPref.isFocusFullBody(),
                userPref.isBeginner(),
                userPref.isIntermediate(),
                userPref.isAdvanced(),
                1,
                userPref.isGoalStrength(),
                userPref.isGoalEndurance(),
                userPref.isGoalFlexibility(),
                userPref.isGoalBalanceCoordination(),
                true
        );
        MvcResult result = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update-preferences")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("user.preferences@email.com", USER_ROLES))
                        .content(objectMapper.writeValueAsString(newUserPreferences))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        UserInfoDto userResponse = objectMapper.readValue(response.getContentAsString(), UserInfoDto.class);
        ApplicationUserPreferences updatedPreferences = applicationUserPreferencesRepository.findById(user.getApplicationUserId());

        assertAll(
                () -> assertNotNull(userResponse),
                () -> assertNotNull(updatedPreferences),
                () -> assertEquals(1, updatedPreferences.getPhysicalDemandLevel()),
                () -> assertTrue(updatedPreferences.isGoalMentalFocus())
        );
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/on-preference", recordedRequest.getPath());
        mockWebServer.shutdown();
    }

    @Test
    void testWhetherUserPreferencesAreSetReturnsTrue() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/preferences-check")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("user.preferences@email.com", USER_ROLES)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        Boolean isSet = objectMapper.readValue(response.getContentAsString(), Boolean.class);

        assertAll(
                () -> assertNotNull(isSet),
                () -> assertTrue(isSet)
        );
    }


    @Test
    void testUnauthorizedRequestGetUserInfoFailsDueToSecurityException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/sila.admin@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", ADMIN_ROLES)))
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("This action is not available", errorsNode.asText())
        );
    }

    @Test
    void testGetUserInfoOfNonExistingUserFailsDueToNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/nonexisting@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andExpect(status().isNotFound())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testGetUserInfoOfBlockedUserThrowsSecurityException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/forbidden.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("forbidden.user@email.com", ADMIN_ROLES)))
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("User forbidden.user@email.com is blocked", errorsNode.asText())
        );
    }

    @Test
    void testGetUserInfoReturnsUserCorrectly() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/sila.admin@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserInfoDto user = objectMapper.readValue(response.getContentAsString(),UserInfoDto.class);

        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals("Administrator", user.firstName()),
                () -> assertEquals("Admin", user.lastName()),
                () -> assertEquals("Admin address", user.location()),
                () -> assertEquals(Gender.MALE, user.gender())
        );
    }

    @Test
    void testDeleteUserSuccessfullyAsAdmin() throws Exception {
        this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/blocked.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/blocked.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andExpect(status().isNotFound())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testDeleteUserWhoIsStudioAdminAndTheirStudioIsApprovedSuccessfully() throws Exception {
        Studio studio = studioRepository.findByStudioAdminEmail("studio2admin@email.com");
        this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/studio2admin@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/studio2admin@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andExpect(status().isNotFound())
                .andReturn();

        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorMessage = responseJson.get("message");
        assertAll(
                () -> assertNotNull(errorMessage),
                () -> assertEquals("Not found", errorMessage.asText())
        );
    }

    @Test
    void testDeleteOwnUserProfileSuccessfully() throws Exception {
        this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/successful.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/successful.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andExpect(status().isNotFound())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testDeleteUnknownUserThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/unknown@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound()).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testUnauthorizedDeleteRequestThrowsSecurityException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/blocked.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isForbidden()).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("This action is not available", errorsNode.asText())
        );
    }

    @Test
    void testBlockedUserTriesToDeleteTheirProfileThrowsSecurityException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/blocked.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("blocked.user@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isForbidden()).andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("User blocked.user@email.com is blocked", errorsNode.asText())
        );
    }

    @Test
    void testDeleteUserThrowsConflictExceptionIfItsAttemptedToDeleteLastAdminAccount() throws Exception {
        this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/sila.admin@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult mvcResult = this.mockMvc.perform(delete(APPLICATION_USER_BASE + "/delete/admin@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isConflict()).andReturn();


        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorMessage = responseJson.get("message");
        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorMessage),
                () -> assertEquals("Conflict occurred", errorMessage.asText()),
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("You are the last admin with access!"))
        );
    }

    @Test
    void testUserProfileUpdateSuccessfully() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/sila.admin@gmail.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        UserInfoDto user = objectMapper.readValue(response.getContentAsString(), UserInfoDto.class);

        UpdateUserInfoDto updatedUser = new UpdateUserInfoDto(
                "Maja",
                user.lastName(),
                user.email(),
                "My Vienna location",
                0,
                0,
                user.gender(),
                user.profileImagePath()
        );

        MvcResult result1 = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response1 = result1.getResponse();

        UserInfoDto userResponse = objectMapper.readValue(response1.getContentAsString(), UserInfoDto.class);

        assertAll(
                () -> assertNotNull(userResponse),
                () -> assertEquals(updatedUser.firstName(), userResponse.firstName()),
                () -> assertEquals(updatedUser.lastName(), userResponse.lastName()),
                () -> assertEquals(updatedUser.email(), userResponse.email()),
                () -> assertEquals(updatedUser.location(), userResponse.location()),
                () -> assertEquals(updatedUser.gender(), userResponse.gender()),
                () -> assertEquals(updatedUser.profileImagePath(), userResponse.profileImagePath())
        );

    }

    @Test
    void testUnauthorizedUpdateUserInfoRequest() throws Exception {
        UserInfoDto updatedUser = new UserInfoDto(
                "Maja",
                "Petojevic",
                "sila.admin@gmail.com",
                "My Vienna location",
                Gender.FEMALE,
                null
        );

        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/update")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("This action is not available", errorsNode.asText())
        );
    }

    @Test
    void testUnblockUserAsAdminSuccessfully() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/unblock/" + "forbidden.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        UserDetailDto userResponse = objectMapper.readValue(response.getContentAsString(), UserDetailDto.class);

        assertAll(
                () -> assertNotNull(userResponse),
                () -> assertEquals("forbidden.user@email.com", userResponse.email()),
                () -> assertFalse(userResponse.isLocked())
        );
    }

    @Test
    void testUnblockUserFailsWithValidationExceptionBecauseUserIsNotBlocked() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/unblock/" + "successful.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("User successful.user@email.com is not blocked"))
        );
    }

    @Test
    void testUnblockUserFailsWithVNotFoundExceptionBecauseUserDoesNotExist() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put(APPLICATION_USER_BASE + "/unblock/" + "randomuser@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testUploadingProfileImageAsUserSuccessfully() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(APPLICATION_USER_BASE + "/upload-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        String imageUrl = response.getContentAsString();

        assertAll(
                () -> assertNotNull(imageUrl),
                () -> assertTrue(imageUrl.contains("assets/user/profile-images")),
                () -> assertTrue(imageUrl.contains("image1.jpg"))
        );
    }

    @Test
    void testUpdatingExistingProfileImageSuccessfully() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );

        this.mockMvc.perform(MockMvcRequestBuilders.multipart(APPLICATION_USER_BASE + "/upload-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk());

        MockMultipartFile newProfileImage = new MockMultipartFile(
                "file",
                "image2.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image2 content".getBytes()
        );

        MvcResult updatedProfileImg = this.mockMvc.perform(MockMvcRequestBuilders.multipart(APPLICATION_USER_BASE + "/upload-profile-image")
                        .file(newProfileImage)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = updatedProfileImg.getResponse();

        String imageUrl = response.getContentAsString();

        assertAll(
                () -> assertNotNull(imageUrl),
                () -> assertTrue(imageUrl.contains("assets/user/profile-images")),
                () -> assertTrue(imageUrl.contains("image2.jpg"))
        );

    }

    @Test
    void testUnauthorizedAccessOnGetAllUsersThrowsForbiddenException() throws Exception {
        this.mockMvc.perform(get(APPLICATION_USER_BASE + "/search?pageIndex=0&pageSize=10"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testAuthorizedSearchAllUsersRetrieves8Users() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/search?pageIndex=0&pageSize=10")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<UserDetailDto> userListDtos = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, UserDetailDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertEquals(8, totalElements),
                () -> {
                    for (UserDetailDto dto : userListDtos) {
                        assertTrue(dto.email().contains("@"));
                        assertNotNull(dto.firstName());
                        assertNotNull(dto.lastName());
                        assertNotNull(dto.isAdmin());
                        assertNotNull(dto.isLocked());
                        assertNotNull(dto.isStudioAdmin());
                    }
                }
        );
    }

    @Test
    void testAuthorizedSearchRetrieves2Admins() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/search?pageIndex=0&pageSize=10&isAdmin=true")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<UserDetailDto> userListDtos = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, UserDetailDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertEquals(2, totalElements),
                () -> {
                    for (UserDetailDto dto : userListDtos) {
                        assertTrue(dto.email().contains("@"));
                        assertNotNull(dto.firstName());
                        assertNotNull(dto.lastName());
                        assertNotNull(dto.isAdmin());
                        assertNotNull(dto.isLocked());
                        assertNotNull(dto.isStudioAdmin());
                    }
                }
        );
    }

    @Test
    void testAuthorizedSearchWithNameParamSAndLastnameUReturnsCorrectSingularUser() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/search?pageIndex=0&pageSize=10&firstName=succ&lastName=u")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<UserDetailDto> userListDtos = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, UserDetailDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertEquals(1, totalElements),
                () -> {
                    for (UserDetailDto dto : userListDtos) {
                        assertEquals("successful.user@email.com", dto.email());
                        assertEquals("Successful", dto.firstName());
                        assertEquals("User", dto.lastName());
                        assertEquals(Boolean.FALSE, dto.isAdmin());
                        assertEquals(Boolean.FALSE, dto.isLocked());
                        assertEquals(Boolean.FALSE, dto.isStudioAdmin());
                        assertEquals("My Location", dto.location());
                        assertEquals(Gender.MALE, dto.gender());
                    }
                }
        );
    }





}
