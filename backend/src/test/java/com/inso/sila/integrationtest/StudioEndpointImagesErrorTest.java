package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.entity.Studio;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
import com.inso.sila.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.inso.sila.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StudioEndpointImagesErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private TestDataGenerator testDataGenerator;

    @MockBean
    private ImageService imageService;

    @Autowired
    private StudioRepository studioRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
        Mockito.reset(imageService);
    }

    @Test
    void createStudioFailsWithFileLargerThan5MB() throws Exception {
        // Prepare a large file (6MB)
        MockMultipartFile largeFile = new MockMultipartFile(
                "profileImageFile",
                "largeImage.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024] // 6MB file
        );

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE)
                        .file(largeFile)
                        .param("name", "Studio Oversized")
                        .param("description", "A studio with a large profile image")
                        .param("location", "Large Studio Location")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "large@studio.com")
                        .param("password", "GoodPass123!")
                        .param("confirmPassword", "GoodPass123!")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("errors");
        List<String> errorMessages = StreamSupport.stream(errorsNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.isArray()),
                () -> assertTrue(errorMessages.contains("File largeImage.jpg is too large to be added. Maximal file size is 5MB."))
        );
    }

    @Test
    void createStudioFailsWithIOExceptionDuringImageUpload() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "Valid image content".getBytes()
        );

        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE)
                        .file(mockFile)
                        .param("name", "Studio IO")
                        .param("description", "A studio that triggers IOException")
                        .param("location", "IOException Studio Location")
                        .param("latitude", "0")
                        .param("longitude", "0")
                        .param("email", "ioexception@studio.com")
                        .param("password", "GoodPass123!")
                        .param("confirmPassword", "GoodPass123!")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode detailNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(detailNode),
                () -> assertEquals("Simulated IO Exception", detailNode.asText())
        );
    }

    @Test
    void updateStudioProfileImageFailsWithIOExceptionDuringImageUpload() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "Valid image content".getBytes()
        );

        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = new ObjectMapper().readTree(responseBody);

        JsonNode messageNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(messageNode),
                () -> assertEquals("Simulated IO Exception", messageNode.asText())
        );
    }

    @Test
    void testUploadLargeProfileImageForStudioFailsWithValidationException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "largeImage.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024] // 6MB file
        );
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(STUDIO_BASE + "/" + studio.getStudioId() + "/update-profile-image")
                        .file(largeFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("errors");
        List<String> errorMessages = StreamSupport.stream(errorsNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.isArray()),
                () -> assertTrue(errorMessages.contains("File largeImage.jpg is too large to be added. Maximal file size is 5MB."))
        );
    }

    @Test
    void testAddInstructorThrowsValidationExceptionDueToUploadingLargeFile() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile largeFile = new MockMultipartFile(
                "profileImage",
                "largeImage.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024] // 6MB file
        );

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE + "/add-instructor/" + studio.getStudioId())
                        .file(largeFile)
                        .param("firstName", "Instructor")
                        .param("lastName", "Instructor Lastname")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("errors");
        List<String> errorMessages = StreamSupport.stream(errorsNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.isArray()),
                () -> assertTrue(errorMessages.contains("File largeImage.jpg is too large to be added. Maximal file size is 5MB."))
        );
    }

    @Test
    void testAddInstructorFailsWithIOException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "Valid image content".getBytes()
        );

        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());

        MvcResult mvcResult = this.mockMvc.perform(multipart(STUDIO_BASE + "/add-instructor/" + studio.getStudioId())
                        .file(mockFile)
                        .param("firstName", "Instructor")
                        .param("lastName", "Instructor Lastname")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode detailNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(detailNode),
                () -> assertEquals("Simulated IO Exception", detailNode.asText())
        );
    }

    @Test
    void testUploadImagesToGalleryFailsWithValidationExceptionOnUploadingLargeImages() throws Exception {
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
                new byte[6 * 1024 * 1024]
        );
        MvcResult mvcResult = this.mockMvc.perform(multipart(HttpMethod.PUT, STUDIO_BASE + "/" + studio.getStudioId() + "/add-gallery-images")
                        .file(image1)
                        .file(image2)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode errorsNode = responseJson.get("errors");
        List<String> errorMessages = StreamSupport.stream(errorsNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();

        assertAll(
                () -> assertNotNull(errorsNode),
                () -> assertTrue(errorsNode.isArray()),
                () -> assertTrue(errorMessages.contains("File image2.jpg is too large to be added. Maximal file size is 5MB."))
        );
    }

    @Test
    void uploadingImagesToGalleryFailsWithIOException() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "Mock Image Content 1".getBytes()
        );
        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());
        MvcResult mvcResult = this.mockMvc.perform(multipart(HttpMethod.PUT, STUDIO_BASE + "/" + studio.getStudioId() + "/add-gallery-images")
                        .file(image1)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("studio2admin@email.com", STUDIO_ADMIN_ROLES))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode detailNode = responseJson.get("detail");

        assertAll(
                () -> assertNotNull(detailNode),
                () -> assertEquals("Simulated IO Exception", detailNode.asText())
        );

    }

}
