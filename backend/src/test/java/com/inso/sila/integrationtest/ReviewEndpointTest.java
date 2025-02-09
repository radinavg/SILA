package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.dto.studio.review.ReviewCreateDto;
import com.inso.sila.endpoint.dto.studio.review.ReviewDto;
import com.inso.sila.entity.Studio;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import com.inso.sila.service.ReviewService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;

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
class ReviewEndpointTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StudioRepository studioRepository;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private ReviewService reviewService;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testAddingReviewSuccessfully() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult mvcResult = this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        mockWebServer.shutdown();
        MockHttpServletResponse response = mvcResult.getResponse();
        ReviewDto createdReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);

        assertAll(
                () -> assertNotNull(createdReview),
                () -> assertEquals("admin@email.com", createdReview.user().email()),
                () -> assertEquals("Great Studio!", createdReview.text()),
                () -> assertEquals(5, createdReview.rating())
        );
    }

    @Test
    void testCreatingReviewFailsWhenBeingAddedToNonExistingStudio() throws Exception {
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult mvcResult = this.mockMvc.perform(post(REVIEW_BASE + "/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testDeleteReviewSuccessfullyAndTryToUpdateItWhichThrowsNotFoundException() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult postResult = this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = postResult.getResponse();
        ReviewDto createdReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);
        mockWebServer.shutdown();
        this.mockMvc.perform(delete(REVIEW_BASE + "/" + createdReview.reviewId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MvcResult mvcUpdate = this.mockMvc.perform(put(REVIEW_BASE + "/" + createdReview.reviewId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        response = mvcUpdate.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("message");
        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testUnauthorizedDeletionOfAReviewThrowsSecurityException() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult postResult = this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = postResult.getResponse();
        ReviewDto createdReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);

        mockWebServer.shutdown();
        MvcResult mvcResult = this.mockMvc.perform(delete(REVIEW_BASE + "/" + createdReview.reviewId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();
        response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.toString().contains("This action is not available"))
        );

    }

    @Test
    void testDeleteNonExistingReviewThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(REVIEW_BASE + "/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }

    @Test
    void testUpdateReviewSuccessfullyAsUserWhoWroteIt() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult postResult = this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = postResult.getResponse();
        ReviewDto createdReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);
        mockWebServer.shutdown();
        ReviewCreateDto newReviewDto = new ReviewCreateDto(
                "This studio got so much worse over time!",
                1
        );
        MvcResult mvcUpdate = this.mockMvc.perform(put(REVIEW_BASE + "/" + createdReview.reviewId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReviewDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        response = mvcUpdate.getResponse();
        ReviewDto updatedReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);
        assertAll(
                () -> assertNotNull(updatedReview),
                () -> assertEquals("This studio got so much worse over time!", updatedReview.text()),
                () -> assertEquals(1, updatedReview.rating()),
                () -> assertEquals("successful.user@email.com", updatedReview.user().email())
        );
    }

    @Test
    void testUnauthorizedAttemptToUpdateReviewThrowsSecurityException() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        MvcResult postResult = this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = postResult.getResponse();
        ReviewDto createdReview = objectMapper.readValue(response.getContentAsString(), ReviewDto.class);
        mockWebServer.shutdown();
        ReviewCreateDto newReviewDto = new ReviewCreateDto(
                "This studio got so much worse over time!",
                1
        );
        MvcResult mvcUpdate = this.mockMvc.perform(put(REVIEW_BASE + "/" + createdReview.reviewId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReviewDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();

        response = mvcUpdate.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.toString().contains("This action is not available"))
        );
    }

    @Test
    void testSortReviewsReturnsListOf2ReviewsForGivenStudio() throws Exception {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        ReviewCreateDto createDto = new ReviewCreateDto(
                "Great Studio!",
                5
        );
        this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        mockWebServer.shutdown();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(reviewService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        this.mockMvc.perform(post(REVIEW_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        mockWebServer.shutdown();
        MvcResult mvcResult = this.mockMvc.perform(get(REVIEW_BASE + "/" + studio.getStudioId() + "?pageIndex=0&pageSize=10")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<ReviewDto> reviews = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, ReviewDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertEquals(2, totalElements),
                () -> {
                    for (ReviewDto dto : reviews) {
                        assertTrue(dto.user().email().contains("@"));
                        assertEquals(5, dto.rating());
                        assertEquals("Great Studio!", dto.text());
                    }
                }
        );
    }

    @Test
    void testSortReviewsThrowsNotFoundExceptionWhenStudioDoesNotExist() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(REVIEW_BASE + "/-9999?pageIndex=0&pageSize=10")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("message");

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertEquals("Not found", errorsNode.asText())
        );
    }
}
