package com.inso.sila.integrationtest;

import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.AlgorithmTestDataGenerator;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.Review;
import com.inso.sila.repository.ApplicationUserPreferencesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioActivityRepository;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Rollback(false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class AlgorithmTest {
    private static final int USER_COUNT = 100;
    private static final int ACTIVITY_COUNT = 100;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    AlgorithmTestDataGenerator algorithmTestDataGenerator;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    StudioActivityRepository studioActivityRepository;

    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/on-preference";
    @Autowired
    private ApplicationUserPreferencesRepository applicationUserPreferencesRepository;

    @BeforeEach
    public void setup() throws IOException {
        algorithmTestDataGenerator.cleanTestData();
        algorithmTestDataGenerator.generateAlgorithmTestData(100,100);

    }

    @Test
    @Transactional
    @Disabled
    void testDataGeneration() {
        // Verify that test data was generated
        long userCount = applicationUserRepository.count();
        long activityCount = studioActivityRepository.count();

        // Assert that data exists
        assertThat(userCount).isPositive();
        assertThat(activityCount).isPositive();
    }

    @Test
    @Transactional
    @Disabled
    void testFriendshipsAssigned() {
        ApplicationUser user = applicationUserRepository.findRandomUser();
        assertThat(user.getFriends()).isNotEmpty();
    }

    @Test
    @Disabled
    @Transactional
    void testUserPreferencesAssigned() {
        ApplicationUser user = applicationUserRepository.findRandomUser();
        ApplicationUserPreferences preferences = user.getPreferences();

        assertAll(
                // Basic assertions to check if preferences are not null
                () -> assertNotNull(preferences),
                // Check if activity setting is not null and has a valid value
                () -> assertThat(preferences.isPrefersIndoor() || preferences.isPrefersOutdoor() || preferences.isPrefersBothIndoorAndOutdoor()).isTrue(),
                // Physical demand level should be within the valid range (1-10)
                () -> assertThat(preferences.getPhysicalDemandLevel()).isPositive().isLessThanOrEqualTo(10),
                // Goal settings - check if at least one is true
                () -> assertThat(preferences.isGoalStrength() || preferences.isGoalEndurance() || preferences.isGoalFlexibility()
                        || preferences.isGoalBalanceCoordination() || preferences.isGoalMentalFocus()).isTrue(),
                // Focus areas - check if at least one is true
                () -> assertThat(preferences.isFocusUpperBody() || preferences.isFocusLowerBody() || preferences.isFocusCore()
                        || preferences.isFocusFullBody()).isTrue(),
                // Climate preferences - check if at least one is true
                () -> assertThat(preferences.isPrefersWarmClimate() || preferences.isPrefersColdClimate()).isTrue(),
                // Skill level - check if at least one is true
                () -> assertThat(preferences.isBeginner() || preferences.isIntermediate() || preferences.isAdvanced()).isTrue(),
                // Team preferences - check if at least one is true
                () -> assertThat(preferences.isPrefersIndividual() || preferences.isPrefersTeam() || preferences.isPrefersWaterBased()).isTrue()
        );
    }

    @Test
    @Transactional
    @Disabled
    void testDataGenerationReviews() {
        // Verify that test data was generated
        long reviewCount = reviewRepository.count();
        long activityCount = studioActivityRepository.count();
        long userCount = applicationUserRepository.count();
        assertThat(reviewCount).isPositive();
        assertThat(activityCount).isPositive();
        assertThat(userCount).isPositive();
    }

    @Test
    @Disabled
    void sendRequestToPythonService() throws IOException, InterruptedException {
        algorithmTestDataGenerator.generateAlgorithmTestData(100, 100);

        HttpClient client = HttpClient.newHttpClient();
        applicationUserPreferencesRepository.getReferenceById(1);
        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PYTHON_SERVICE_URL))
                .header("X-Test-Mode", "true")
                .GET()
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // You can check headers, for example, to see if they match the expected value
        HttpHeaders headers = response.headers();
        Map<String, List<String>> allHeaders = headers.map();
        System.out.println("Response Headers: " + allHeaders);

        assertEquals(200, response.statusCode(), "Expected status 200");
    }

    @Test
    @Disabled
    void sendRequestToPythonServiceWith100UsersPerf() throws IOException, InterruptedException {
        algorithmTestDataGenerator.generateAlgorithmTestData(100, 100);

        HttpClient client = HttpClient.newHttpClient();
        applicationUserPreferencesRepository.getReferenceById(1);
        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PYTHON_SERVICE_URL))
                .header("X-Test-Mode", "true")
                .GET()
                .build();

        long startTime = System.nanoTime();
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;
        // You can check headers, for example, to see if they match the expected value
        HttpHeaders headers = response.headers();
        Map<String, List<String>> allHeaders = headers.map();
        System.out.println("Response Headers: " + allHeaders);

        assertThat(durationInMillis).isLessThan(10000);
        assertEquals(200, response.statusCode(), "Expected status 200");
    }

    @Test
    @Disabled
    void sendRequestToPythonServiceWith1000UsersPerf() throws IOException, InterruptedException {
        algorithmTestDataGenerator.generateAlgorithmTestData(1000, 100);

        HttpClient client = HttpClient.newHttpClient();
        applicationUserPreferencesRepository.getReferenceById(1);
        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PYTHON_SERVICE_URL))
                .header("X-Test-Mode", "true")
                .GET()
                .build();

        long startTime = System.nanoTime();
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;
        // You can check headers, for example, to see if they match the expected value
        HttpHeaders headers = response.headers();
        Map<String, List<String>> allHeaders = headers.map();
        System.out.println("Response Headers: " + allHeaders);

        assertThat(durationInMillis).isLessThan(10000);
        assertEquals(200, response.statusCode(), "Expected status 200");
    }

    @Test
    @Disabled
    void sendRequestToPythonServiceWith10000UsersPerf() throws IOException, InterruptedException {
        algorithmTestDataGenerator.generateAlgorithmTestData(10000, 100);

        HttpClient client = HttpClient.newHttpClient();
        applicationUserPreferencesRepository.getReferenceById(1);
        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PYTHON_SERVICE_URL))
                .header("X-Test-Mode", "true")
                .GET()
                .build();

        long startTime = System.nanoTime();
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;
        // You can check headers, for example, to see if they match the expected value
        HttpHeaders headers = response.headers();
        Map<String, List<String>> allHeaders = headers.map();
        System.out.println("Response Headers: " + allHeaders);

        assertThat(durationInMillis).isLessThan(10000);
        assertEquals(200, response.statusCode(), "Expected status 200");
    }


    @Test
    @Disabled
    void sendRequestToPythonServiceWith100UsersPerfAndCheckDB() throws IOException, InterruptedException {
        algorithmTestDataGenerator.generateAlgorithmTestData(100, 100);

        HttpClient client = HttpClient.newHttpClient();
        applicationUserPreferencesRepository.getReferenceById(1);
        // Build the GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PYTHON_SERVICE_URL))
                .header("X-Test-Mode", "true")
                .GET()
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // You can check headers, for example, to see if they match the expected value
        HttpHeaders headers = response.headers();
        Map<String, List<String>> allHeaders = headers.map();
        System.out.println("Response Headers: " + allHeaders);

        String sql = "SELECT COUNT(*) FROM recommendation_cluster_recommended_activities"; // Replace with your actual table name
        Integer rowCount = jdbcTemplate.queryForObject(sql, Integer.class);

        assertThat(rowCount).isGreaterThan(0);
        assertEquals(200, response.statusCode(), "Expected status 200");
    }






}
