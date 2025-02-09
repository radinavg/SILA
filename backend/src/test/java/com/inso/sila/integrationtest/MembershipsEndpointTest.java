package com.inso.sila.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inso.sila.SilaApplication;
import com.inso.sila.basetest.TestDataGenerator;
import com.inso.sila.config.properties.SecurityProperties;
import com.inso.sila.endpoint.dto.studio.membership.MembershipDto;
import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Membership;
import com.inso.sila.entity.Studio;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.repository.StudioRepository;
import com.inso.sila.security.JwtTokenizer;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.inso.sila.basetest.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MembershipsEndpointTest {

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
    private MembershipRepository membershipRepository;
    @Autowired
    private StudioRepository studioRepository;
    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @BeforeEach
    public void setup() {
        testDataGenerator.clearTestData();
        testDataGenerator.generateTestData();
    }

    @Test
    void testGetMembershipsForUserReturnsList() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(MEMBERSHIP_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", List.of("ROLE_USER"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<MembershipDto> memberships = objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        assertAll(
                () -> assertNotNull(memberships),
                () -> assertFalse(memberships.isEmpty())
        );
    }


    @Test
    void testHasMembershipForStudioReturnsTrue() throws Exception {
        Membership membership = membershipRepository.findAll().getFirst();

        MvcResult mvcResult = this.mockMvc.perform(get(MEMBERSHIP_BASE + "/hasMembershipForStudio/" + membership.getStudio().getStudioId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", List.of("ROLE_USER"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        boolean hasMembership = objectMapper.readValue(response.getContentAsString(), Boolean.class);

        assertTrue(hasMembership);
    }

    @Test
    void testHasMembershipForStudioReturnsFalseForNonExistingStudio() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/memberships/hasMembershipForStudio/-9999")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", List.of("ROLE_USER"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        boolean hasMembership = objectMapper.readValue(response.getContentAsString(), Boolean.class);

        assertFalse(hasMembership);
    }

    @Test
    void testAddMembershipForUserSuccessfully() throws Exception {
        Membership membership = membershipRepository.findAll().get(1);

        MembershipDto membershipDto = new MembershipDto(
                membership.getMembershipId(),
                membership.getName(),
                membership.getDuration(),
                membership.getMinDuration(),
                membership.getPrice()
        );

        MvcResult mvcResult = this.mockMvc.perform(post(MEMBERSHIP_BASE)
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", List.of("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membershipDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        MembershipDto createdMembership = objectMapper.readValue(response.getContentAsString(), MembershipDto.class);

        assertAll(
                () -> assertNotNull(createdMembership),
                () -> assertEquals(membership.getName(), createdMembership.name()),
                () -> assertEquals(membership.getPrice(), createdMembership.price())
        );
    }

    @Test
    void testDeleteMembershipFromUserSuccessfully() throws Exception {
        Membership membership = membershipRepository.findAll().getFirst();

        // Perform the DELETE operation
        MvcResult mvcResult = this.mockMvc.perform(delete(MEMBERSHIP_BASE + "/" + membership.getMembershipId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("successful.user@email.com", List.of("ROLE_USER"))))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        assertTrue(membershipRepository.existsById(membership.getMembershipId()), "Membership should still exist in the repository.");
    }



    @Test
    void testDeleteMembershipThatDoesNotExist() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(delete(MEMBERSHIP_BASE + "/-1")
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"))))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertTrue(response.getContentAsString().isEmpty());
    }
}


