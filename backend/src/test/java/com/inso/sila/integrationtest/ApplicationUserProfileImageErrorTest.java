package com.inso.sila.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
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

import static com.inso.sila.basetest.TestData.ADMIN_ROLES;
import static com.inso.sila.basetest.TestData.APPLICATION_USER_BASE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApplicationUserProfileImageErrorTest {

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

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
        Mockito.reset(imageService);
    }

    @Test
    void uploadImagesFailsWithFileLargerThan5MB() throws Exception {
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "largeImage.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                new byte[6 * 1024 * 1024]  // 6MB file
        );

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(APPLICATION_USER_BASE + "/upload-profile-image")
                        .file(largeFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
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
    void uploadImagesFailsWithIOException() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "image1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image1 content".getBytes()
        );

        // Simulate IOException
        Mockito.doThrow(new IOException("Simulated IO Exception"))
                .when(imageService)
                .saveImage(Mockito.any(), Mockito.any());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart(APPLICATION_USER_BASE + "/upload-profile-image")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("sila.admin@gmail.com", ADMIN_ROLES)))
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



}
