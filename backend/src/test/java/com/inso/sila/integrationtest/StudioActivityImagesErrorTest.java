package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.entity.Studio;
import com.inso.sila.repository.StudioActivityRepository;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.inso.sila.basetest.TestData.ACTIVITY_BASE;
import static com.inso.sila.basetest.TestData.STUDIO_ADMIN_ROLES;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StudioActivityImagesErrorTest {
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

    @Autowired
    private StudioActivityRepository studioActivityRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
        Mockito.reset(imageService);
    }

    @Test
    void testAddActivityToAStudioFailsWithValidationExceptionForImagesBiggerThan5MB() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile largeFile = new MockMultipartFile(
                "profileImageFile",
                "largeImage.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024] // 6MB file
        );
        MvcResult mvcResult = this.mockMvc.perform(multipart(ACTIVITY_BASE)
                        .file(largeFile)
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
    void testAddActivityToStudioFailsWithIOExceptionDuringImageUpload() throws Exception {
        Studio studio = studioRepository.findByApprovedTrue().getFirst();
        MockMultipartFile mockFile = new MockMultipartFile(
                "profileImageFile",
                "profile.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "Valid image content".getBytes()
        );

        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());
        MvcResult mvcResult = this.mockMvc.perform(multipart(ACTIVITY_BASE)
                        .file(mockFile)
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
