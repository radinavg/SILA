package com.inso.sila.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.dto.studio.faqs.FaqsDto;
import com.inso.sila.endpoint.dto.studio.instructor.InstructorDto;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.endpoint.dto.studio.studio.*;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Studio;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import com.inso.sila.service.StudioService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
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

import static com.inso.sila.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StudioEndpointTest {

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
    private StudioService studioService;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testAddStudioAndItsAdminSuccessfully() throws Exception {
        MockMultipartFile profileImageFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );


        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE)
                        .file("profileImageFile", profileImageFile.getBytes())
                        .param("name", "Studio Maja")
                        .param("description", "Description")
                        .param("location", "Location Maja Studio")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "majastudio@email.com")
                        .param("password", "GoodPass1234!")
                        .param("confirmPassword", "GoodPass1234!")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        CreatedStudioDto createdStudio = objectMapper.readValue(response.getContentAsString(), CreatedStudioDto.class);

        assertAll(
                () -> assertNotNull(createdStudio),
                () -> assertEquals("Studio Maja", createdStudio.name()),
                () -> assertEquals("Description", createdStudio.description()),
                () -> assertEquals("Location Maja Studio", createdStudio.location())
        );
    }

    @Test
    void testInvalidStudioCreateBecauseOfUnprocessablePasswordFormat() throws Exception {
        MockMultipartFile profileImageFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE)
                        .file("profileImageFile", profileImageFile.getBytes())
                        .param("name", "Studio Maja")
                        .param("description", "Description")
                        .param("location", "Location Maja Studio")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "majastudio@email.com")
                        .param("password", "GoodPass")
                        .param("confirmPassword", "GoodPass")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
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
                () -> assertEquals(2, errorsNode.size(), "Errors array should contain 3 items"),
                () -> assertTrue(errorsNode.toString().contains("Password must contain at least one number"), "Missing error for number"),
                () -> assertTrue(errorsNode.toString().contains("Password must contain at least one special character"), "Missing error for special character")
        );
    }

    @Test
    void testAddStudioOnNonUniqueLocationThrowsConflictException() throws Exception {
        MockMultipartFile profileImageFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE)
                        .file("profileImageFile", profileImageFile.getBytes())
                        .param("name", "Studio Maja")
                        .param("description", "Description")
                        .param("location", "My studio location 2")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "majastudio@email.com")
                        .param("password", "GoodPass!123")
                        .param("confirmPassword", "GoodPass!123")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("There already exists a studio on given location"))
        );
    }

    @Test
    void testInvalidStudioCreateBecauseOfConflictExceptionUserWithSameEmailExists() throws Exception {
        MockMultipartFile profileImageFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE)
                        .file("profileImageFile", profileImageFile.getBytes())
                        .param("name", "Studio Maja")
                        .param("description", "Description")
                        .param("location", "Location Maja Studio")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "admin@email.com")
                        .param("password", "GoodPass123!")
                        .param("confirmPassword", "GoodPass123!")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isConflict())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorMessage = responseJson.get("message");
        JsonNode errorsNode = responseJson.get("errors");

        assertAll(
                () -> assertNotNull(errorMessage),
                () -> assertEquals("Couldn't create user", errorMessage.asText()),
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.isArray(), "Errors node should be an array"),
                () -> assertEquals(1, errorsNode.size(), "Errors array should contain 1 item"),
                () -> assertTrue(errorsNode.toString().contains("User with the given email already exists"))
        );
    }

    @Test
    void testApprovingStudioCorrectly() throws Exception {
        Studio studio = studioRepository.findByApprovedFalse().getFirst();
        StudioApprovalDto approvalDto = new StudioApprovalDto(studio.getStudioId(), true);
        this.mockMvc.perform(put(STUDIO_BASE + "/approve")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(approvalDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Studio approved = studioRepository.findById(studio.getStudioId()).orElseThrow(() -> new NotFoundException("Studio not found"));
        assertAll(
                () -> assertNotNull(approved),
                () -> assertTrue(approved.isApproved())
        );
    }

    @Test
    void testApprovingStudioFailsBecauseItsTriedToApproveNonExistentStudio() throws Exception {
        StudioApprovalDto approvalDto = new StudioApprovalDto(-9999L, true);
        MvcResult mvcResult = this.mockMvc.perform(put(STUDIO_BASE + "/approve")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(approvalDto))
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
    void testSuccessfullyUpdatingStudioNameLocationAndDescription() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        StudioUpdateDto updateDto = new StudioUpdateDto(
                "Fitness Center Brigittenau",
                "Find your way to shine with Fitness Center Brigittenau!",
                "Klosterneuburgerstrasse 23/1",
                0,
                0
        );
        MvcResult mvcResult = this.mockMvc.perform(put(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto updatedStudio = objectMapper.readValue(response.getContentAsString(), StudioDto.class);

        assertAll(
                () -> assertNotNull(updatedStudio),
                () -> assertEquals("Fitness Center Brigittenau", updatedStudio.name()),
                () -> assertEquals("Find your way to shine with Fitness Center Brigittenau!", updatedStudio.description()),
                () -> assertEquals("Klosterneuburgerstrasse 23/1", updatedStudio.location())
        );
    }

    @Test
    void testUpdateStudioCausesValidationExceptionBecauseOfMissingParameters() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        StudioUpdateDto updateDto = new StudioUpdateDto(
                null,
                null,
                "Klosterneuburgerstrasse 23/1",
                0,
                0
        );
        MvcResult mvcResult = this.mockMvc.perform(put(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
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
                () -> assertEquals(2, errorsNode.size(), "Errors array should contain 2 items"),
                () -> assertTrue(errorsNode.toString().contains("Studio name cannot be blank")),
                () -> assertTrue(errorsNode.toString().contains("Description cannot be blank"))
        );
    }

    @Test
    void testUpdateStudioWithUnauthorizedUserFailsWithSecurityException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        StudioUpdateDto updateDto = new StudioUpdateDto(
                "Fitness Center Brigittenau",
                "Find your way to shine with Fitness Center Brigittenau!",
                "Klosterneuburgerstrasse 23/1",
                0,
                0
        );
        MvcResult mvcResult = this.mockMvc.perform(put(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", STUDIO_ADMIN_ROLES))
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);

        JsonNode errorsNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(errorsNode, "Errors node should not be null"),
                () -> assertTrue(errorsNode.toString().contains("This action is not available"))
        );
    }

    @Test
    void testUpdatingNonExistingStudioFailsWithNotFoundException() throws Exception {
        StudioUpdateDto updateDto = new StudioUpdateDto(
                "Fitness Center Brigittenau",
                "Find your way to shine with Fitness Center Brigittenau!",
                "Klosterneuburgerstrasse 23/1",
                0,
                0
        );
        MvcResult mvcResult = this.mockMvc.perform(put(STUDIO_BASE + "/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", STUDIO_ADMIN_ROLES))
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
    void testUpdateStudioProfileImageSuccessfullyWhenNoImageExists() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto updatedStudio = objectMapper.readValue(response.getContentAsString(), StudioDto.class);

        assertAll(
                () -> assertNotNull(updatedStudio),
                () -> assertTrue(updatedStudio.profileImage().path().contains("assets/studio/profile-image")),
                () -> assertEquals("image1.jpg", updatedStudio.profileImage().name())
        );
    }

    @Test
    void testUpdateStudioProfileImageSuccessfullyWhenImageIsOverwritten() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockMultipartFile newProfileImage = new MockMultipartFile(
                "file",
                "image2.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image2 content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(newProfileImage)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto updatedStudio = objectMapper.readValue(response.getContentAsString(), StudioDto.class);

        assertAll(
                () -> assertNotNull(updatedStudio),
                () -> assertTrue(updatedStudio.profileImage().path().contains("assets/studio/profile-images/" + studio.getStudioId())),
                () -> assertEquals("image2.jpg", updatedStudio.profileImage().name())
        );
    }

    @Test
    void testUpdateStudioProfileImageFailsDueToUnauthorisedUploadAttempt() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", STUDIO_ADMIN_ROLES)))
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
    void testUpdateStudioProfileImageThrowsNotFoundExceprtionForNonExistingStudio() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + -9999L + "/update-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", STUDIO_ADMIN_ROLES)))
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
    void testGetAllStudiosReturnsOneApprovedStudio() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        List<StudioDto> studios = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        assertAll(
                () -> assertNotNull(studios),
                () -> assertEquals(1, studios.size()),
                () -> {
                    for (StudioDto dto : studios) {
                        assertEquals("Studio 2", dto.name());
                        assertEquals("Studio 2 Description", dto.description());
                        assertEquals("My studio location 2", dto.location());
                        assertTrue(dto.approved());
                    }
                }
        );
    }

    @Test
    void testNotApprovedStudiosReturnsOneStudio() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/notApproved")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        List<StudioDto> studios = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        assertAll(
                () -> assertNotNull(studios),
                () -> assertEquals(1, studios.size()),
                () -> {
                    for (StudioDto dto : studios) {
                        assertEquals("Studio 1", dto.name());
                        assertEquals("Studio 1 Description", dto.description());
                        assertEquals("My studio location", dto.location());
                        assertFalse(dto.approved());
                    }
                }
        );
    }

    @Test
    void testGetStudioByIdReturnsCorrectStudio() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto studioDto = objectMapper.readValue(response.getContentAsString(), StudioDto.class);
        assertAll(
                () -> assertEquals("Studio 2", studioDto.name()),
                () -> assertEquals("Studio 2 Description", studioDto.description()),
                () -> assertEquals("My studio location 2", studioDto.location()),
                () -> assertTrue(studioDto.approved())
        );
    }

    @Test
    void testGetStudioByNonExistingIdThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/-9999")
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
    void testDeleteNotApprovedStudioAndItsAdminUser() throws Exception {
        Studio studio = studioRepository.findByApprovedFalse().getFirst();
        ApplicationUser admin = studio.getStudioAdmin();
        this.mockMvc.perform(delete(STUDIO_BASE + "/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk()).andReturn();
        MvcResult mvcResult = this.mockMvc.perform(get(APPLICATION_USER_BASE + "/info/" + admin.getEmail())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound()).andReturn();
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
    void testDeleteNotExistingStudioReturnsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(STUDIO_BASE + "/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isNotFound()).andReturn();
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
    void testAddFaqToStudioSuccessfully() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        FaqsDto faqsDto = new FaqsDto(
                null,
                "Question",
                "Answer"
        );

        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/faqs")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faqsDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        FaqsDto addedFaq = objectMapper.readValue(response.getContentAsString(), FaqsDto.class);

        assertAll(
                () -> assertNotNull(addedFaq),
                () -> assertEquals("Question", addedFaq.question()),
                () -> assertEquals("Answer", addedFaq.answer())
        );
    }

    @Test
    void testAddFaqToStudioThrowsNotFoundExceptionWhenStudioDoesntExist() throws Exception {
        FaqsDto faqsDto = new FaqsDto(
                null,
                "Question",
                "Answer"
        );

        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/-9999/faqs")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faqsDto)))
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
    void testAddingFaqsThrowsValidationExceptionBecauseOfMissingArguments() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        FaqsDto faqsDto = new FaqsDto(
                null,
                "",
                "Answer"
        );

        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/faqs")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faqsDto)))
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
                () -> assertTrue(errorsNode.toString().contains("Question cannot be blank"))
        );
    }

    @Test
    void testAddFaqUnauthorizedAccessThrowsSecurityException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        FaqsDto faqsDto = new FaqsDto(
                null,
                "Question",
                "Answer"
        );

        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/faqs")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio1admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faqsDto)))
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
    void testAddMembershipToStudioSuccessfully() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MembershipDto membershipDto = new MembershipDto(
                null,
                "Three months trail",
                3,
                3,
                100f
        );
        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/memberships")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membershipDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        MembershipDto membership = objectMapper.readValue(response.getContentAsString(), MembershipDto.class);
        assertAll(
                () -> assertNotNull(membership),
                () -> assertEquals("Three months trail", membership.name()),
                () -> assertEquals(3, membership.duration()),
                () -> assertEquals(3, membership.minDuration()),
                () -> assertEquals(100f, membership.price())
        );
    }

    @Test
    void testAddMembershipToStudioThrowsNotFoundExceptionWhenStudioDoesntExist() throws Exception {
        MembershipDto membershipDto = new MembershipDto(
                null,
                "Three months trail",
                3,
                3,
                100f
        );
        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/-9999/memberships")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membershipDto)))
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
    void testAddMembershipFailsDueToUnauthorizedAccess() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MembershipDto membershipDto = new MembershipDto(
                null,
                "Three months trail",
                3,
                3,
                100f
        );
        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/memberships")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio1admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membershipDto)))
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
    void testSuccessfullyAddFavoriteStudio() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(studioService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());
        this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/add-favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        mockWebServer.shutdown();
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        List<StudioDto> studios = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        assertAll(
                () -> assertNotNull(studios),
                () -> assertEquals(1, studios.size()),
                () -> {
                    for (StudioDto dto : studios) {
                        assertEquals("Studio 2", dto.name());
                        assertEquals("Studio 2 Description", dto.description());
                        assertEquals("My studio location 2", dto.location());
                        assertTrue(dto.approved());
                    }
                }
        );
    }

    @Test
    void testAddFavouriteStudioThrowsNotFoundExceptionWhenLikingNonExistingStudio() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(STUDIO_BASE + "/-9999/add-favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
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
    void testRemovingFavouriteStudioFromUserSuccessfully() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockResponse = "Mock Response from Python Service";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));

        ReflectionTestUtils.setField(studioService, "webClient",
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());

        this.mockMvc.perform(post(STUDIO_BASE + "/" + studio.getStudioId() + "/add-favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();
        mockWebServer.shutdown();
        MvcResult mvcResult = this.mockMvc.perform(delete(STUDIO_BASE + "/" + studio.getStudioId() + "/remove-favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto studioDto = objectMapper.readValue(response.getContentAsString(), StudioDto.class);
        assertAll(
                () -> assertNotNull(studioDto),
                () -> assertFalse(studioDto.isFavouriteForUser())
        );

    }

    @Test
    void testRemovingFavouriteStudioThatDoesNotExistThrowsNotFoundException() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(STUDIO_BASE + "/-99999/remove-favourite")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", USER_ROLES)))
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
    void testSearchByNameAndLocationReturns1StudiosWhenSearchParamsAreAllNull() throws Exception {
        // there is only one approved studio
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/search?pageIndex=0&pageSize=10")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("admin@email.com", ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        JsonNode rootNode = objectMapper.readTree(response.getContentAsString());

        JsonNode contentNode = rootNode.get("content");
        long totalElements = rootNode.get("totalElements").asLong();


        List<StudioInfoDto> studioInfo = StreamSupport.stream(contentNode.spliterator(), false)
                .map(node -> {
                    try {
                        return objectMapper.treeToValue(node, StudioInfoDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping JSON node to UserListDto", e);
                    }
                }).toList();

        assertAll(
                () -> assertEquals(1, totalElements),
                () -> {
                    for (StudioInfoDto dto : studioInfo) {
                        assertTrue(dto.email().contains("@"));
                        assertNotNull(dto.name());
                        assertNotNull(dto.description());
                        assertNotNull(dto.location());
                        assertNotNull(dto.email());
                    }
                }
        );
    }

    @Test
    void testGetStudioIdByCurrentlyLoggedInStudioAdmin() throws Exception {
        MvcResult studioResult = this.mockMvc.perform(get(STUDIO_BASE + "/getStudioByAdmin/studio2admin@email.com")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = studioResult.getResponse().getContentAsString();
        long studioId = Long.parseLong(responseBody);
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/" + studioId)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto studioDto = objectMapper.readValue(response.getContentAsString(), StudioDto.class);
        assertAll(
                () -> assertEquals("Studio 2", studioDto.name()),
                () -> assertEquals("Studio 2 Description", studioDto.description()),
                () -> assertEquals("My studio location 2", studioDto.location()),
                () -> assertTrue(studioDto.approved())
        );
    }

    @Test
    void testIsCurrentUserAdminReturnsTrue() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/isAdmin/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        boolean isAdmin = Boolean.parseBoolean(responseBody);
        assertTrue(isAdmin);
    }

    @Test
    void testIsCurrentUserAdminReturnsFalse() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/isAdmin/" + studio.getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        boolean isAdmin = Boolean.parseBoolean(responseBody);
        assertFalse(isAdmin);
    }

    @Test
    void testIsCurrentUserAdminThrowsNotFoundExceptionForNonExistingStudio() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(STUDIO_BASE + "/isAdmin/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successfull.user@email.com", STUDIO_ADMIN_ROLES)))
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
    void testAddInstructorToStudioSuccessfully() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile instructorImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE + "/add-instructor/" + studio.getStudioId())
                        .file(instructorImage)
                        .param("firstName", "Instructor")
                        .param("lastName", "Instructor Lastname")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        InstructorDto createdInstructor = objectMapper.readValue(response.getContentAsString(), InstructorDto.class);

        assertAll(
                () -> assertNotNull(createdInstructor),
                () -> assertEquals("Instructor", createdInstructor.firstName()),
                () -> assertEquals("Instructor Lastname", createdInstructor.lastName()),
                () -> assertEquals("profile.jpg", createdInstructor.profileImage().name())
        );
    }

    @Test
    void testAddInstructorToStudioThrowsNotFoundExceptionForNonExistingStudio() throws Exception {
        MockMultipartFile instructorImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE + "/add-instructor/-9999")
                        .file(instructorImage)
                        .param("firstName", "Instructor")
                        .param("lastName", "Instructor Lastname")
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
    void testUnauthorizedAttemptToAddInstructorThrowsSecurityException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile instructorImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "Mock Image Content".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE + "/add-instructor/" + studio.getStudioId())
                        .file(instructorImage)
                        .param("firstName", "Instructor")
                        .param("lastName", "Instructor Lastname")
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
    void testAddGalleryImagesToStudioSuccessfully() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();

        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "Mock Image Content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "image2.jpg",
                "image/jpeg",
                "Mock Image Content 2".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(HttpMethod.PUT, STUDIO_BASE + "/" + studio.getStudioId() + "/add-gallery-images")
                        .file(image1)
                        .file(image2)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        StudioDto updatedStudio = objectMapper.readValue(response.getContentAsString(), StudioDto.class);

        assertAll(
                () -> assertNotNull(updatedStudio),
                () -> assertEquals(studio.getStudioId(), updatedStudio.studioId()),
                () -> assertTrue(updatedStudio.galleryImages().size() >= 2), // Ensure the gallery images are updated
                () -> assertTrue(updatedStudio.galleryImages().stream().anyMatch(img -> img.name().equals("image1.jpg"))),
                () -> assertTrue(updatedStudio.galleryImages().stream().anyMatch(img -> img.name().equals("image2.jpg")))
        );
    }

    @Test
    void testUploadGalleryImagesThrowsNotFoundExceptionForNonExistingStudio() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "Mock Image Content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "image2.jpg",
                "image/jpeg",
                "Mock Image Content 2".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(HttpMethod.PUT, STUDIO_BASE + "/-9999/add-gallery-images")
                        .file(image1)
                        .file(image2)
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
    void testUploadGalleryAsUnauthorizedUserImagesThrowsSecurityException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();

        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "Mock Image Content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "image2.jpg",
                "image/jpeg",
                "Mock Image Content 2".getBytes()
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(HttpMethod.PUT, STUDIO_BASE + "/" + studio.getStudioId() + "/add-gallery-images")
                        .file(image1)
                        .file(image2)
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


}
