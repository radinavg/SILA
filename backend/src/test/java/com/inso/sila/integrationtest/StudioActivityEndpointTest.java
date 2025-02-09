package com.inso.sila.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.dto.studio.studio.StudioActivityListDto;
import com.inso.sila.endpoint.dto.studio.studio.StudioForActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityDto;
import com.inso.sila.endpoint.dto.studioactivity.StudioActivityTypeSearchResponseDto;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import com.inso.sila.service.StudioActivityService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.inso.sila.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StudioActivityEndpointTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StudioActivityRepository studioActivityRepository;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private StudioActivityService studioActivityService;
    @Autowired
    private StudioRepository studioRepository;



    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testFindStudioActivityByIdReturnsRightStudioActivity() throws Exception {
        StudioActivity activity = studioActivityRepository.findAll().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/" + activity.getStudioActivityId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioActivityDto activityDto = objectMapper.readValue(response.getContentAsString(), StudioActivityDto.class);
        assertAll(
                () -> assertNotNull(activityDto),
                () -> assertEquals(30, activityDto.capacity()),
                () -> assertEquals("Basketball Skills Workshop", activityDto.name()),
                () -> assertEquals("Hone your basketball techniques with professional coaches.", activityDto.description()),
                () -> assertEquals(90, activityDto.duration()),
                () -> assertEquals("Serena", activityDto.instructor().firstName()),
                () -> assertEquals("Williams", activityDto.instructor().lastName())
        );
    }

    @Test
    void testFindStudioActivityThrowsNotFoundExceptionWhenStudioActivityDoesNotExist() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
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
    void testUpdateStudioActivitySuccessfully() throws Exception {
        StudioActivity activity = studioActivityRepository.findAll().getFirst();
        StudioActivityDto updateDto  = new StudioActivityDto(
                activity.getStudioActivityId(),
                null,
                "New Activity name",
                activity.getDescription(),
                activity.getDateTime(),
                60f,
                activity.getPrice(),
                ActivityType.GROUP_ACTIVITIES,
                10,
                null,
                null,
                null
        );
        MvcResult mvcResult = this.mockMvc.perform(put(ACTIVITY_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioActivityDto activityDto = objectMapper.readValue(response.getContentAsString(), StudioActivityDto.class);
        assertAll(
                () -> assertNotNull(activityDto),
                () -> assertEquals("New Activity name", activityDto.name()),
                () -> assertEquals("Hone your basketball techniques with professional coaches.", activityDto.description()),
                () -> assertEquals(60, activityDto.duration()),
                () -> assertEquals(ActivityType.GROUP_ACTIVITIES, activityDto.type())
        );
    }

    @Test
    void testUpdateNonExistingActivityThrowsNotFoundException() throws Exception {
        StudioActivityDto updateDto  = new StudioActivityDto(
                -9999L,
                null,
                "New Activity name",
                "hello",
                LocalDateTime.now().plusDays(2),
                60f,
                50,
                ActivityType.GROUP_ACTIVITIES,
                10,
                null,
                null,
                null
        );
        MvcResult mvcResult = this.mockMvc.perform(put(ACTIVITY_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
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
    void testUpdateStudioActivityThrowsSecurityExceptionOnUnauthorizedAccess() throws Exception {
        StudioActivity activity = studioActivityRepository.findAll().getFirst();
        StudioActivityDto updateDto  = new StudioActivityDto(
                activity.getStudioActivityId(),
                null,
                "New Activity name",
                activity.getDescription(),
                activity.getDateTime(),
                60f,
                activity.getPrice(),
                ActivityType.GROUP_ACTIVITIES,
                10,
                null,
                null,
                null
        );
        MvcResult mvcResult = this.mockMvc.perform(put(ACTIVITY_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio1admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.toString().contains("This action is not available"))
        );
    }

    @Test
    void testAddActivityToAStudioSuccessfully() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(studioActivityService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile activityImage = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(ACTIVITY_BASE)
                        .file(activityImage)
                        .param("name", "Activity 1")
                        .param("description", "Description")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("duration", "60")
                        .param("price", "30")
                        .param("type", "YOGA")
                        .param("studioId", studio.getStudioId().toString())
                        .param("capacity", "20")
                        .param("skillLevel", "BEGINNER")
                        .param("equipment", "true")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        StudioActivityListDto createdActivity = objectMapper.readValue(response.getContentAsString(), StudioActivityListDto.class);

        assertAll(
                () -> assertNotNull(createdActivity),
                () -> assertEquals("Activity 1", createdActivity.name()),
                () -> assertEquals("Description", createdActivity.description()),
                () -> assertEquals(30, createdActivity.price()),
                () -> assertEquals(60, createdActivity.duration()),
                () -> assertEquals("profile.jpg", createdActivity.profileImage().name())
        );

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/on-preference", recordedRequest.getPath());
        mockWebServer.shutdown();
    }

    @Test
    void testAttemptToAddActivityToNonExistingStudioThrowsNotFoundException() throws Exception {
        MockMultipartFile activityImage = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );
        MvcResult mvcResult = this.mockMvc.perform(multipart(ACTIVITY_BASE)
                        .file(activityImage)
                        .param("name", "Activity 1")
                        .param("description", "Description")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("duration", "60")
                        .param("price", "30")
                        .param("type", "YOGA")
                        .param("studioId", "-9999")
                        .param("capacity", "20")
                        .param("skillLevel", "BEGINNER")
                        .param("equipment", "true")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
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
    void testUnauthorizedAttemptToAddActivityToStudioOfWhichCurrentUserIsNotAdminThrowsSecurityException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile activityImage = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );
        MvcResult mvcResult = this.mockMvc.perform(multipart(ACTIVITY_BASE)
                        .file(activityImage)
                        .param("name", "Activity 1")
                        .param("description", "Description")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("duration", "60")
                        .param("price", "30")
                        .param("type", "YOGA")
                        .param("studioId", studio.getStudioId().toString())
                        .param("capacity", "20")
                        .param("skillLevel", "BEGINNER")
                        .param("equipment", "true")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio1admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("detail");
        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.toString().contains("This action is not available"))
        );
    }

    @Test
    void testFindStudioByActivityIDReturnsCorrectStudio() throws Exception {
        StudioActivity studioActivity = studioRepository.findByApprovedTrue().getFirst().getStudioActivities().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/activityId/" + studioActivity.getStudioActivityId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioForActivityDto studioDto = objectMapper.readValue(response.getContentAsString(), StudioForActivityDto.class);
        assertAll(
                () -> assertEquals("Studio 2", studioDto.name()),
                () -> assertEquals("Studio 2 Description", studioDto.description()),
                () -> assertEquals("My studio location 2", studioDto.location())
        );

    }

    @Test
    void testAttemptToFindStudioByNonExistingActivityIdThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/activityId/-9999")
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
    void testGetActivityTypesReturns7ActivityTypes() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/types")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        List<String> types = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        assertAll(
                () -> assertNotNull(types),
                () -> assertEquals(7, types.size())
        );
    }

    @Test
    void getActivityByTypeBallSportReturns2Activities() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/exploreTypes?activityType=BALL_SPORTS&pageIndex=0&pageSize=10")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<StudioActivityTypeSearchResponseDto> activities = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, StudioActivityTypeSearchResponseDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertNotNull(activities),
                () -> assertEquals(2, totalElements),
                () -> assertEquals("Basketball Skills Workshop", activities.getFirst().name()),
                () -> assertEquals("Volleyball Practice", activities.get(1).name())
        );
    }

    @Test
    void testGetActivitiesOfStudioReturnsTwoCorrectActivities() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/studio/" + studio.getStudioId() + "/activities")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        List<StudioActivityListDto> activitiesDto = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});

        assertAll(
                () -> assertNotNull(activitiesDto),
                () -> assertEquals(2, activitiesDto.size()),
                () -> assertEquals("Basketball Skills Workshop", activitiesDto.getFirst().name()),
                () -> assertEquals("Volleyball Practice", activitiesDto.get(1).name())
        );
    }

    @Test
    void testGetStudioActivitiesOfNonExistingStudioThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(ACTIVITY_BASE + "/studio/-9999/activities")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
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
}
