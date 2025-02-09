package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.dto.requests.ActivityInvitationDto;
import com.inso.sila.endpoint.dto.requests.FriendRequestDto;
import com.inso.sila.endpoint.dto.user.UserInfoDto;
import com.inso.sila.endpoint.mapper.StudioActivityMapper;
import com.inso.sila.endpoint.mapper.UserMapper;
import com.inso.sila.enums.Gender;
import com.inso.sila.enums.RequestStatus;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.inso.sila.basetest.TestData.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InvitationsEndpointTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StudioActivityMapper studioActivityMapper;
    @Autowired
    private StudioActivityRepository studioActivityRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testAddFriendRequestReturnsCreatedRequest() throws Exception {
        FriendRequestDto friendRequestDto = FriendRequestDto.builder()
                .from(null)
                .to(userMapper.userToUserInfoDto(applicationUserRepository.findByEmail("successful.user@email.com")))
                .status(RequestStatus.PENDING)
                .requestDateTime(LocalDateTime.now())
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/invitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(friendRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        FriendRequestDto responseDto = objectMapper.readValue(response.getContentAsString(), FriendRequestDto.class);

        assertAll(
                () -> assertNotNull(responseDto, "Response should not be null"),
                () -> assertEquals(DEFAULT_USER, responseDto.from().email(), "Sender email should match"),
                () -> assertEquals(friendRequestDto.to().email(), responseDto.to().email(), "Receiver email should match"),
                () -> assertEquals(friendRequestDto.status(), responseDto.status(), "Request status should match"),
                () -> assertNotNull(responseDto.requestDateTime().toLocalDate(), "Request date should not be null")
        );
    }

    @Test
    void testAddFriendRequestForNonExistingUserReturnsNotFoundException() throws Exception {
        FriendRequestDto friendRequestDto = FriendRequestDto.builder()
                .from(null)
                .to(UserInfoDto.builder()
                        .email("non_existing_user@email.com")  // Email of the non-existing user
                        .firstName("non_existing_user")
                        .gender(Gender.MALE)
                        .lastName("non_existing_user")
                        .build())
                .status(RequestStatus.PENDING)
                .requestDateTime(LocalDateTime.now())
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/invitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(friendRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertTrue(response.getStatus() == 404, "Expected 404 status, but got: " + response.getStatus());
    }

    @Test
    void testAddActivityInvitationReturnsCreatedInvitation() throws Exception {
        ActivityInvitationDto activityInvitationDto = ActivityInvitationDto.builder()
                .from(null)
                .to(userMapper.userToUserInfoDto(applicationUserRepository.findByEmail("successful.user@email.com")))
                .studioActivity(studioActivityMapper.entityToDto(studioActivityRepository.findAll().getFirst()))
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/invitation/activityInvitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(activityInvitationDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ActivityInvitationDto responseDto = objectMapper.readValue(response.getContentAsString(), ActivityInvitationDto.class);

        assertAll(
                () -> assertNotNull(responseDto, "Response should not be null"),
                () -> assertEquals(DEFAULT_USER, responseDto.from().email(), "Sender email should match"),
                () -> assertEquals(activityInvitationDto.to().email(), responseDto.to().email(), "Receiver email should match"),
                () -> assertEquals(activityInvitationDto.studioActivity().studioActivityId(), responseDto.studioActivity().studioActivityId(), "Activity ID should match")
        );
    }

    @Test
    void testAddActivityInvitationForNonExistingUserReturnsNotFoundException() throws Exception {
        UserInfoDto nonExistingUser = UserInfoDto.builder()
                .email("non_existing_user@email.com")
                .firstName("non_existing_user")
                .gender(Gender.MALE)
                .lastName("non_existing_user")
                .build();


        ActivityInvitationDto activityInvitationDto = ActivityInvitationDto.builder()
                .from(null)
                .to(nonExistingUser)
                .studioActivity(studioActivityMapper.entityToDto(studioActivityRepository.findAll().getFirst())) // Ensure the activity exists
                .build();

       this.mockMvc.perform(post("/api/v1/invitation/activityInvitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(activityInvitationDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void testGetUserSentFriendRequestsReturnsList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/invitation/my_friend_invitations/{email}", "successful.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()) // Expecting a 200 OK status
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response.getContentAsString(), "Response should not be null");
    }

    @Test
    void testGetUserSentFriendRequestsForNonExistingUserReturnsEmptyList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/invitation/my_friend_invitations/{email}", "non_existing.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()) // Expecting a 200 OK status
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals("[]", response.getContentAsString(), "Response should be an empty list");
    }

    @Test
    void testGetUserSentActivityInvitationsReturnsList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/invitation/my_activity_invitations/{email}", "successful.user@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()) // Expecting a 200 OK status
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response.getContentAsString(), "Response should not be null");
    }

    @Test
    void testAddActivityInvitationForNonExistingActivityReturnsNotFoundException() throws Exception {
        ActivityInvitationDto activityInvitationDto = ActivityInvitationDto.builder()
                .from(null)
                .to(userMapper.userToUserInfoDto(applicationUserRepository.findByEmail("successful.user@email.com")))
                .studioActivity(null)  // Non-existing activity
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/invitation/activityInvitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(activityInvitationDto)))
                .andDo(print())
                .andExpect(status().isNotFound())  // Expecting a 404 Not Found status
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(404, response.getStatus(), "Expected 404 status, but got: " + response.getStatus());
    }


    @Test
    void testAddDuplicateFriendRequestReturnsConflict() throws Exception {
        FriendRequestDto friendRequestDto = FriendRequestDto.builder()
                .from(userMapper.userToUserInfoDto(applicationUserRepository.findByEmail(DEFAULT_USER)))
                .to(userMapper.userToUserInfoDto(applicationUserRepository.findByEmail("successful.user@email.com")))
                .status(RequestStatus.PENDING)
                .requestDateTime(LocalDateTime.now())
                .build();

        this.mockMvc.perform(post("/api/v1/invitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(friendRequestDto)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post("/api/v1/invitation")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(friendRequestDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }


}
