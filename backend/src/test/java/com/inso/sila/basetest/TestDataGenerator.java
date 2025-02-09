package com.inso.sila.basetest;

import com.inso.sila.entity.*;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.enums.Gender;
import com.inso.sila.repository.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.inso.sila.basetest.TestData.*;
import static org.antlr.v4.runtime.tree.xpath.XPath.findAll;

@Component
public class TestDataGenerator {

    @Getter
    private static final TestDataGenerator instance = new TestDataGenerator();
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Autowired
    ApplicationUserRepository userRepository;
    @Autowired
    StudioRepository studioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private FavouriteStudiosRepository favouriteStudiosRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    FriendRequestRepository friendRequestRepository;
    @Autowired
    ActivityInvitationRepository activityInvitationRepository;
    @Autowired
    StudioActivityPreferencesRepository preferencesRepository;
    @Autowired
    ActivityTypeAttributesRepository attributesRepository;
    @Autowired
    StudioActivityRepository studioActivityRepository;
    @Autowired
    private StudioActivityPreferencesRepository studioActivityPreferencesRepository;
    @Autowired
    MembershipRepository membershipRepository;
    @Autowired
    private ReviewRepository reviewRepository;


    // Private constructor to prevent instantiation
    private TestDataGenerator() {
    }

    public void generateTestData() {
        LOG.info("Generate test data...");
        // Add your generation methods here
        generateUsers();
        generateAStudioAndItsAdmin();
        generateAttributes();
    }

    public void clearTestData() {
        LOG.info("Clearing data...");
        friendRequestRepository.deleteAll();
        activityInvitationRepository.deleteAll();
        favouriteStudiosRepository.deleteAll();
        reviewRepository.deleteAll();
        studioRepository.deleteAll();
        attributesRepository.deleteAll();
        studioActivityPreferencesRepository.deleteAll();
        studioActivityRepository.deleteAll();
        userRepository.deleteAll();
    }


    private void generateUsers() {

        ApplicationUser loginSuccessfulUser = ApplicationUser.builder()
                .firstName("Successful")
                .lastName("User")
                .email("successful.user@email.com")
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("GoodPassword123")) // Encode the password
                .isAdmin(false)
                .isStudioAdmin(false)
                .location("My Location")
                .isLocked(false)
                .memberships(new HashSet<>())
                .build();

        userRepository.save(loginSuccessfulUser);

        ApplicationUser loginForbiddenUser = ApplicationUser.builder()
                .firstName("Forbidden")
                .lastName("User")
                .email("forbidden.user@email.com")
                .password(passwordEncoder.encode("GoodPassword123")) // Encode the password
                .isAdmin(false)
                .isStudioAdmin(false)
                .gender(Gender.FEMALE)
                .isLocked(true)
                .location("My Location")
                .build();

        userRepository.save(loginForbiddenUser);
        ApplicationUser defaultUser = ApplicationUser.builder()
                .firstName("Default")
                .lastName("User")
                .email(DEFAULT_USER)
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("password")) // Encode the password
                .isAdmin(true)
                .isStudioAdmin(false)
                .isLocked(false)
                .location("My Location")
                .build();

        userRepository.save(defaultUser);

        ApplicationUser admin = ApplicationUser.builder()
                .firstName("Administrator")
                .lastName("Admin")
                .email("sila.admin@gmail.com")
                .location("Admin address")
                .password(passwordEncoder.encode("password")) // Encode the password
                .isAdmin(true)
                .isStudioAdmin(false)
                .gender(Gender.MALE)
                .isLocked(false)
                .build();

        userRepository.save(admin);

        ApplicationUser blockedUser = ApplicationUser.builder()
                .firstName("Blocked")
                .lastName("User")
                .email("blocked.user@email.com")
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("password"))
                .isAdmin(false)
                .isStudioAdmin(false)
                .isLocked(true)
                .location("My Location")
                .build();

        userRepository.save(blockedUser);

        ApplicationUser userWithPreferences = ApplicationUser.builder()
                .firstName("User")
                .lastName("Preferences")
                .email("user.preferences@email.com")
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("password"))
                .isAdmin(false)
                .isStudioAdmin(false)
                .isLocked(false)
                .preferencesSet(true)
                .location("My Location")
                .build();
        userWithPreferences = userRepository.save(userWithPreferences);
        ApplicationUserPreferences applicationUserPreferences = ApplicationUserPreferences.builder()
                .user(userWithPreferences)
                .prefersIndividual(false)
                .prefersTeam(true)
                .prefersWaterBased(false)
                .prefersIndoor(true)
                .prefersOutdoor(false)
                .prefersBothIndoorAndOutdoor(false)
                .prefersWarmClimate(true)
                .prefersColdClimate(false)
                .rainCompatibility(false)
                .windSuitability(false)
                .focusUpperBody(true)
                .focusCore(true)
                .focusFullBody(true)
                .isBeginner(false)
                .isIntermediate(true)
                .isAdvanced(false)
                .physicalDemandLevel(5)
                .goalStrength(true)
                .goalEndurance(true)
                .goalFlexibility(false)
                .goalBalanceCoordination(true)
                .goalMentalFocus(false).build();
        userWithPreferences.setPreferences(applicationUserPreferences);
        userRepository.save(userWithPreferences);

        Membership membership = Membership.builder()
                .name("Premium Membership")
                .price(50F)
                .applicationUsers(new HashSet<>())
                .build();

        membershipRepository.save(membership);

        loginSuccessfulUser.getMemberships().add(membership);
        membership.getApplicationUsers().add(loginSuccessfulUser);

        userRepository.save(loginSuccessfulUser);
        membershipRepository.save(membership);

    }

    void generateAStudioAndItsAdmin() {
        ApplicationUser studioAdminUser = ApplicationUser.builder()
                .firstName("Studio Admin Of")
                .lastName("Studio 1")
                .email("studio1admin@email.com")
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("password"))
                .isAdmin(false)
                .isStudioAdmin(true)
                .isLocked(false)
                .location("My Location")
                .build();

        studioAdminUser = userRepository.saveAndFlush(studioAdminUser);
        Studio studio = Studio.builder()
                .name("Studio 1")
                .description("Studio 1 Description")
                .location("My studio location")
                .approved(false)
                .studioActivities(new ArrayList<>())
                .memberships(new ArrayList<>())
                .faqs(new ArrayList<>())
                .likedFromApplicationUsers(new ArrayList<>())
                .studioAdmin(studioAdminUser)
                .build();

        studioRepository.save(studio);

        ApplicationUser studioAdminUser2 = ApplicationUser.builder()
                .firstName("Studio Admin Of")
                .lastName("Studio 2")
                .email("studio2admin@email.com")
                .gender(Gender.MALE)
                .password(passwordEncoder.encode("password"))
                .isAdmin(false)
                .isStudioAdmin(true)
                .isLocked(false)
                .location("My Location")
                .build();

        studioAdminUser2 = userRepository.saveAndFlush(studioAdminUser2);

        Instructor instructor1 = Instructor.builder()
                .firstName("Serena")
                .lastName("Williams")
                .profileImage(ProfileImage.builder()
                        .name("serena_williams_tennis")
                        .path("https://hips.hearstapps.com/hmg-prod/images/gettyimages-1155421342.jpg?crop=1xw:1.0xh;center,top&resize=1200:*")
                        .build())
                .build();
        instructorRepository.save(instructor1);


        Instructor instructor2 = Instructor.builder()
                .firstName("Dwayne")
                .lastName("Johnson")
                .profileImage(ProfileImage.builder()
                        .name("dwayne_johnson_workout")
                        .path("https://m.media-amazon.com/images/M/MV5BOWUzNzIzMzQtNzMxYi00OWRiLTlhZjEtZTRjYWVkYzI4ZjMwXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg")
                        .build())
                .build();

        instructorRepository.save(instructor2);


        Studio studio2 = Studio.builder()
                .name("Studio 2")
                .description("Studio 2 Description")
                .location("My studio location 2")
                .approved(true)
                .studioActivities(List.of(
                        StudioActivity.builder()
                                .name("Basketball Skills Workshop")
                                .description("Hone your basketball techniques with professional coaches.")
                                .dateTime(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
                                .duration(90.0f)
                                .price(25.0f)
                                .instructor(instructor1)
                                .profileImage(ProfileImage.builder()
                                        .name("basketball_skills_workshop")
                                        .path("https://media.istockphoto.com/id/1347653184/photo/young-basketball-player-on-practice-session-youth-basketball-team-bouncing-balls-on-sports.jpg?s=612x612&w=0&k=20&c=Nk3VBCUviP6Tr15ZYf6zAA6hqiWFdWAfuU4Kywweyk4=")
                                        .build())
                                .capacity(30)
                                .type(ActivityType.BALL_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Volleyball Practice")
                                .description("A fun volleyball session for all skill levels.")
                                .dateTime(LocalDateTime.now().plusDays(4).withHour(10).withMinute(30))
                                .duration(60.0f)
                                .price(18.0f)
                                .instructor(instructor2)
                                .profileImage(ProfileImage.builder()
                                        .name("volleyball_practice")
                                        .path("https://media.istockphoto.com/id/489699302/photo/women-spiking-and-blocking-a-volleyball.jpg?s=612x612&w=0&k=20&c=eSnMWORi8fjVZVNwmmUSvIWRVDgEQRfmVVgVD2uFo0E=")
                                        .build())
                                .capacity(30)
                                .type(ActivityType.BALL_SPORTS)
                                .build()
                ))
                .memberships(new ArrayList<>())
                .faqs(new ArrayList<>())
                .likedFromApplicationUsers(new ArrayList<>())
                .studioAdmin(studioAdminUser2)
                .build();

        studioRepository.save(studio2);

        Membership membership = membershipRepository.findAll().getFirst();
        membership.setStudio(studio);
        studio.getMemberships().add(membership);

        membershipRepository.save(membership);
        studioRepository.save(studio);

        Membership membership2 = Membership.builder()
                .name("Premium Membership 2")
                .price(50F)
                .applicationUsers(new HashSet<>())
                .build();

        membership2.setStudio(studio2);
        studio2.getMemberships().add(membership2);
        membershipRepository.save(membership2);
        studioRepository.save(studio2);
    }

    private void generateAttributes() {
        StudioActivityPreferences preferences = createDefaultPreferencesForYoga();
        preferencesRepository.save(preferences);

        ActivityTypeAttributes activityTypeAttributes = ActivityTypeAttributes.builder()
                .activityType(ActivityType.YOGA)
                .attributes(preferences)
                .build();

        attributesRepository.save(activityTypeAttributes);
        LOG.info("Generated attributes for activity type: YOGA");
    }

    private StudioActivityPreferences createDefaultPreferencesForYoga() {
        StudioActivityPreferences preferences = new StudioActivityPreferences();

        // Default values for YOGA
        preferences.setIndividual(true);
        preferences.setTeam(false);
        preferences.setWaterBased(false);
        preferences.setIndoor(true);
        preferences.setOutdoor(false);
        preferences.setBothIndoorAndOutdoor(false);
        preferences.setSuitableWarmClimate(true);
        preferences.setSuitableColdClimate(false);
        preferences.setRainCompatibility(true);
        preferences.setWindSuitability(true);
        preferences.setInvolvesUpperBody(true);
        preferences.setInvolvesLowerBody(true);
        preferences.setInvolvesCore(true);
        preferences.setInvolvesFullBody(true);
        preferences.setBeginner(false);
        preferences.setIntermediate(false);
        preferences.setAdvanced(false);
        preferences.setPhysicalDemandLevel(5);
        preferences.setGoalStrength(false);
        preferences.setGoalEndurance(false);
        preferences.setGoalFlexibility(true);
        preferences.setGoalBalanceCoordination(true);
        preferences.setGoalMentalFocus(true);

        return preferences;
    }

}
