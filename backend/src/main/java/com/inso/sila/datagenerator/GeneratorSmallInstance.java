package com.inso.sila.datagenerator;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.FriendRequest;
import com.inso.sila.entity.GalleryImage;
import com.inso.sila.entity.Instructor;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.entity.StudioActivityPreferences;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.enums.Gender;
import com.inso.sila.enums.RequestStatus;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FriendRequestRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioActivityPreferencesRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Profile({"datagen-small"})
@Component
public class GeneratorSmallInstance {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserRepository userRepository;
    private final StudioRepository studioRepository;
    private final StudioActivityRepository studioActivityRepository;
    private final StudioActivityPreferencesRepository preferencesRepository;
    private final ReviewRepository reviewRepository;
    private final WebClient webClient;
    private static final String CLUSTERING_API = "http://datascience:5000";
    private String password = "password";
    private final ApplicationUserRepository applicationUserRepository;
    private final FriendRequestRepository friendRequestRepository;


    public GeneratorSmallInstance(ApplicationUserRepository userRepository, StudioRepository studioRepository,
                                  StudioActivityRepository studioActivityRepository,
                                  StudioActivityPreferencesRepository preferencesRepository, ReviewRepository reviewRepository,
                                  WebClient.Builder builder, ApplicationUserRepository applicationUserRepository, FriendRequestRepository friendRequestRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studioRepository = studioRepository;
        this.studioActivityRepository = studioActivityRepository;
        this.preferencesRepository = preferencesRepository;
        this.reviewRepository = reviewRepository;
        this.webClient = builder.baseUrl(CLUSTERING_API).build();
        this.applicationUserRepository = applicationUserRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.password = passwordEncoder.encode(password);
    }

    @PostConstruct
    private void generateData() {
        LOG.info("Generating data...");
        if (!userRepository.findAll().isEmpty()) {
            LOG.info("Users already generated");
        } else {
            LOG.info("Generating user entries...");
            generateUsers();
            generateUsersWhoPreferWaterActivities();
            generateUsersWhoPreferCombatSports();
            generateStudiosData();
            generateRandomBookings();
            generateStudioReviews();
            LOG.info("Generating initial clustering...");
            webClient.get()
                    .uri("/on-preference")
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))) // Retry 3 times with a 2s delay
                    .doOnSuccess(response -> LOG.info("Response received: {}", response))
                    .doOnError(error -> LOG.error("Error calling /on-preference: {}", error.getMessage()))
                    .subscribe();

        }
    }

    public void generateUsers() {
        ApplicationUser defaultAdmin = ApplicationUser.builder()
                .isAdmin(Boolean.TRUE)
                .isStudioAdmin(Boolean.FALSE)
                .password(password)
                .email("admin@email.com")
                .firstName("admin")
                .lastName("admin")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Mariahilfer Straße 12, Vienna, Austria")
                .build();

        ApplicationUser testAdmin1 = ApplicationUser.builder()
                .isAdmin(Boolean.TRUE)
                .isStudioAdmin(Boolean.FALSE)
                .password(password)
                .email("test.admin.1@sila.test")
                .firstName("Nikola")
                .lastName("Lukic")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Kärntner Straße 21, Vienna, Austria")
                .build();

        ApplicationUser testAdmin2 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.FALSE)
                .password(password)
                .email("marijanapetojevic@gmail.com")
                .firstName("Marijana")
                .lastName("Petojevic")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.FEMALE)
                .location("Praterstraße 45, Vienna, Austria")
                .build();

        ApplicationUser userUser = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.FALSE)
                .password(password)
                .email("user@email.com")
                .firstName("user")
                .lastName("user")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .longitude(48.2081F)
                .latitude(16.3713F)
                .gender(Gender.MALE)
                .location("Opernring 5, Vienna, Austria")
                .build();

        ApplicationUser stiliyanUser = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.FALSE)
                .password(password)
                .email("stiliyan@email.com")
                .firstName("Stiliyan")
                .lastName("Valkanov")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Brigittenauer Lände 224, Vienna, Austria")
                .build();

        FriendRequest friendRequest = FriendRequest.builder()
                .from(testAdmin2)
                .to(stiliyanUser)
                .status(RequestStatus.PENDING)
                .requestDateTime(LocalDateTime.now())
                .build();



        userRepository.saveAll(Arrays.asList(defaultAdmin, testAdmin1, testAdmin2, userUser, stiliyanUser));
        friendRequestRepository.save(friendRequest);
    }

    public void generateUsersWhoPreferWaterActivities() {
        List<String> firstNames = Arrays.asList(
                "Emma", "Olivia", "Ava", "Isabella", "Sophia",
                "Mia", "Charlotte", "Amelia", "Evelyn", "Abigail"
        );

        List<String> lastNames = Arrays.asList(
                "Smith", "Johnson", "Williams", "Brown", "Jones",
                "Garcia", "Miller", "Davis", "Rodriguez", "Martinez"
        );

        List<String> domains = Arrays.asList(
                "gmail.com", "outlook.com", "yahoo.at"
        );

        for (int i = 0; i < 10; i++) {
            // Vienna coordinates 48.2081° N, 16.3713° E
            float latitude = 48.2081F;
            float longitude = 16.3713F;
            float random = (float) (Math.random() * 0.1F - 0.05F);
            ApplicationUser user = ApplicationUser.builder()
                    .isAdmin(Boolean.FALSE)
                    .isStudioAdmin(Boolean.FALSE)
                    .password(password)
                    .email(firstNames.get(i % 30).toLowerCase()
                            .concat(".")
                            .concat(lastNames.get(i % 30).toLowerCase())
                            .concat(String.valueOf(i))
                            .concat("@")
                            .concat(domains.get(i % domains.size())))
                    .firstName(firstNames.get(i % 30))
                    .lastName(lastNames.get(i % 30))
                    .isLocked(false)
                    .loginAttempts(1)
                    .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                    .location("Street " + (i + 1) + ", Vienna")
                    .latitude(latitude + random)
                    .longitude(longitude + random)
                    .preferencesSet(true)
                    .build();

            user = userRepository.save(user);
            ApplicationUserPreferences applicationUserPreferences = ApplicationUserPreferences.builder()
                    .user(user)
                    .prefersIndividual(false)
                    .prefersTeam(true)
                    .prefersWaterBased(true)
                    .prefersIndoor(false)
                    .prefersOutdoor(false)
                    .prefersBothIndoorAndOutdoor(true)
                    .prefersWarmClimate(true)
                    .prefersColdClimate(false)
                    .rainCompatibility(false)
                    .windSuitability(false)
                    .focusUpperBody(false)
                    .focusLowerBody(false)
                    .focusCore(false)
                    .focusFullBody(true)
                    .isBeginner(true)
                    .isIntermediate(true)
                    .isAdvanced(true)
                    .physicalDemandLevel(5)
                    .goalStrength(false)
                    .goalEndurance(false)
                    .goalFlexibility(false)
                    .goalBalanceCoordination(true)
                    .goalMentalFocus(false).build();
            user.setPreferences(applicationUserPreferences);
            userRepository.save(user);
        }
    }

    public void generateUsersWhoPreferCombatSports() {
        List<String> firstNames = Arrays.asList(
                "Aubrey", "Addison", "Ellie", "Stella", "Natalie",
                "Zoe", "Leah", "Hazel", "Violet", "Aurora"
        );

        List<String> lastNames = Arrays.asList(
                "Walker", "Young", "Allen", "King", "Wright",
                "Scott", "Torres", "Nguyen", "Hill", "Flores"
        );

        List<String> domains = Arrays.asList(
                "gmail.com", "outlook.com", "yahoo.at"
        );

        for (int i = 0; i < 10; i++) {
            // Vienna coordinates 48.2081° N, 16.3713° E
            float latitude = 48.2081F;
            float longitude = 16.3713F;
            float random = (float) (Math.random() * 0.1F - 0.05F);
            ApplicationUser user = ApplicationUser.builder()
                    .isAdmin(Boolean.FALSE)
                    .isStudioAdmin(Boolean.FALSE)
                    .password(password)
                    .email(firstNames.get(i % 30).toLowerCase()
                            .concat(".")
                            .concat(lastNames.get(i % 30).toLowerCase())
                            .concat(String.valueOf(i))
                            .concat("@")
                            .concat(domains.get(i % domains.size())))
                    .firstName(firstNames.get(i % 30))
                    .lastName(lastNames.get(i % 30))
                    .isLocked(false)
                    .loginAttempts(1)
                    .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                    .location("Street " + (i + 1) + ", Vienna")
                    .latitude(latitude + random)
                    .longitude(longitude + random)
                    .preferencesSet(true)
                    .build();

            user = userRepository.save(user);
            ApplicationUserPreferences applicationUserPreferences = ApplicationUserPreferences.builder()
                    .user(user)
                    .prefersIndividual(true)
                    .prefersTeam(false)
                    .prefersWaterBased(false)
                    .prefersIndoor(true)
                    .prefersOutdoor(false)
                    .prefersBothIndoorAndOutdoor(false)
                    .prefersWarmClimate(true)
                    .prefersColdClimate(false)
                    .rainCompatibility(false)
                    .windSuitability(false)
                    .focusUpperBody(true)
                    .focusLowerBody(true)
                    .focusCore(true)
                    .focusFullBody(true)
                    .isBeginner(false)
                    .isIntermediate(true)
                    .isAdvanced(true)
                    .physicalDemandLevel(10)
                    .goalStrength(true)
                    .goalEndurance(true)
                    .goalFlexibility(false)
                    .goalBalanceCoordination(true)
                    .goalMentalFocus(true).build();
            user.setPreferences(applicationUserPreferences);
            userRepository.save(user);
        }
    }

    private StudioActivityPreferences createDefaultPreferencesForActivityType(ActivityType activityType) {
        StudioActivityPreferences preferences = new StudioActivityPreferences();

        switch (activityType) {
            case COMBAT_SPORTS:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(false);
                preferences.setSuitableColdClimate(true);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(10);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(true);
                break;

            case WATER_SPORTS:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(true);
                preferences.setIndoor(false);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(true);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(false);
                preferences.setInvolvesUpperBody(false);
                preferences.setInvolvesLowerBody(false);
                preferences.setInvolvesCore(false);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(true);
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(5);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(false);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            default:
                preferences.setIndividual(false);
                preferences.setTeam(false);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(false);
                preferences.setSuitableColdClimate(false);
                preferences.setRainCompatibility(false);
                preferences.setWindSuitability(false);
                preferences.setInvolvesUpperBody(false);
                preferences.setInvolvesLowerBody(false);
                preferences.setInvolvesCore(false);
                preferences.setInvolvesFullBody(false);
                preferences.setBeginner(false);
                preferences.setIntermediate(false);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(1);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(false);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(false);
                break;
        }
        preferencesRepository.save(preferences);
        return preferences;
    }

    private List<Instructor> getInstructors() {
        // Create and return a list of 10 famous instructors with real profile images
        return List.of(
                Instructor.builder()
                        .firstName("Emma")
                        .lastName("Watson")
                        .profileImage(ProfileImage.builder()
                                .name("emma_watson_activism")
                                .path("https://resizing.flixster.com/-XZAfHZM39UwaGJIFWKAE8fS0ak=/v3/t/assets/247026_v9_bc.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Chris")
                        .lastName("Hemsworth")
                        .profileImage(ProfileImage.builder()
                                .name("chris_hemsworth_fitness")
                                .path("https://cdn.britannica.com/92/215392-050-96A4BC1D/Australian-actor-Chris-Hemsworth-2019.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Serena")
                        .lastName("Williams")
                        .profileImage(ProfileImage.builder()
                                .name("serena_williams_tennis")
                                .path("https://hips.hearstapps.com/hmg-prod/images/gettyimages-1155421342.jpg?crop=1xw:1.0xh;center,top&resize=1200:*")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Dwayne")
                        .lastName("Johnson")
                        .profileImage(ProfileImage.builder()
                                .name("dwayne_johnson_workout")
                                .path("https://m.media-amazon.com/images/M/MV5BOWUzNzIzMzQtNzMxYi00OWRiLTlhZjEtZTRjYWVkYzI4ZjMwXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Gisele")
                        .lastName("Bündchen")
                        .profileImage(ProfileImage.builder()
                                .name("gisele_bundchen_fashion")
                                .path("https://cdn.britannica.com/63/222563-050-B9CD770B/Brazilian-model-Gisele-Bundchen-2016.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("David")
                        .lastName("Beckham")
                        .profileImage(ProfileImage.builder()
                                .name("david_beckham_soccer")
                                .path("https://ntvb.tmsimg.com/assets/assets/501949_v9_bb.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Michelle")
                        .lastName("Obama")
                        .profileImage(ProfileImage.builder()
                                .name("michelle_obama_activism")
                                .path("https://images1.penguinrandomhouse.com/author/149907")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Usain")
                        .lastName("Bolt")
                        .profileImage(ProfileImage.builder()
                                .name("usain_bolt_sprinting")
                                .path("https://i0.gmx.at/image/512/36619512,pd=1,f=size-l/usain-bolt.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("Beyoncé")
                        .lastName("Knowles")
                        .profileImage(ProfileImage.builder()
                                .name("beyonce_knowles_music")
                                .path("https://elle.dk/wp-content/uploads/2021/10/mandagsmuse-beyonce.jpg")
                                .build())
                        .build(),
                Instructor.builder()
                        .firstName("LeBron")
                        .lastName("James")
                        .profileImage(ProfileImage.builder()
                                .name("lebron_james_basketball")
                                .path("https://a.espncdn.com/combiner/i?img=/i/headshots/nba/players/full/1966.png")
                                .build())
                        .build()
        );
    }

    private void generateStudiosData() {

        List<Instructor> instructors = getInstructors();

        Studio combatArena = Studio.builder()
                .name("Combat Arena Vienna")
                .description("A combate fitness center in Vienna.")
                .location("Schönbrunner Schloßstraße 47, 1130 Vienna, Austria")
                .latitude(48.186550f)
                .longitude(16.31364f)
                .approved(true)
                .instructors(List.of(instructors.get(2), instructors.get(3), instructors.get(9)))
                .profileImage(ProfileImage.builder()
                        .name("dynamic_fitness_hub")
                        .path("https://as1.ftcdn.net/v2/jpg/03/29/60/84/1000_F_329608479_vP9nFK795X8lWmoTa8DPhMgoewQ7U1lG.jpg")
                        .build())
                .galleryImages(List.of(
                        GalleryImage.builder()
                                .name("gym_2")
                                .path("https://wholesale.rdxsports.com/hubfs/Modern%20Fitness%20Center.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("gym_3")
                                .path("https://t4.ftcdn.net/jpg/04/65/54/67/360_F_465546726_bZ6mANmm6WzzAonwDWzAnmIvGxct7ak5.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("gym_4")
                                .path("https://i.pinimg.com/originals/c7/f2/ec/c7f2ec4483a1448c4af627022212eeae.jpg")
                                .build()
                ))
                .studioActivities(List.of(
                        StudioActivity.builder()
                                .name("Boxing")
                                .description("Boxing training.")
                                .dateTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0))
                                .duration(45.0f)
                                .price(20.0f)
                                .instructor(instructors.get(2))
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.COMBAT_SPORTS))
                                .profileImage(ProfileImage.builder()
                                        .name("hiit_blast")
                                        .path("https://media.self.com/photos/5c10255b2f04d8625a2fbb64/master/pass/women-with-dumbbells.jpg")
                                        .build())
                                .capacity(15)
                                .type(ActivityType.COMBAT_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Mui Thai")
                                .description("Mui Thai training.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0))
                                .duration(60.0f)
                                .price(22.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.COMBAT_SPORTS))
                                .instructor(instructors.get(3))
                                .profileImage(ProfileImage.builder()
                                        .name("group_strength_training")
                                        .path("https://lifthousefitness.com/wp-content/uploads/2021/02/shutterstock_1049628212-1024x683.png")
                                        .build())
                                .capacity(30)
                                .type(ActivityType.COMBAT_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Combat Training")
                                .description("Learn the basics of combat sports.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(19).withMinute(0))
                                .duration(90.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.COMBAT_SPORTS))
                                .price(25.0f)
                                .instructor(instructors.get(9))
                                .profileImage(ProfileImage.builder()
                                        .name("combat_training")
                                        .path("https://i0.wp.com/www.fightfitnesscenter.net/wp-content/uploads/2016/08/nik-fekete5-1038x576.jpg?ssl=1")
                                        .build())
                                .capacity(25)
                                .type(ActivityType.COMBAT_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Fame boxing")
                                .description("Learn the basics of combat sports.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(19).withMinute(0))
                                .duration(90.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.COMBAT_SPORTS))
                                .price(25.0f)
                                .instructor(instructors.get(9))
                                .profileImage(ProfileImage.builder()
                                        .name("combat_training")
                                        .path("https://i0.wp.com/www.fightfitnesscenter.net/wp-content/uploads/2016/08/nik-fekete5-1038x576.jpg?ssl=1")
                                        .build())
                                .capacity(25)
                                .type(ActivityType.COMBAT_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Wrestling")
                                .description("Learn the basics of combat sports.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(19).withMinute(0))
                                .duration(90.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.COMBAT_SPORTS))
                                .price(25.0f)
                                .instructor(instructors.get(9))
                                .profileImage(ProfileImage.builder()
                                        .name("combat_training")
                                        .path("https://i0.wp.com/www.fightfitnesscenter.net/wp-content/uploads/2016/08/nik-fekete5-1038x576.jpg?ssl=1")
                                        .build())
                                .capacity(25)
                                .type(ActivityType.COMBAT_SPORTS)
                                .build()

                ))
                .studioAdmin(null)
                .build();


        Studio aquaticStudio = Studio.builder()
                .name("AquaFit Wellness Center")
                .description("A center specializing in water-based activities for fitness and fun.")
                .location("Opernring 2, 1010 Vienna, Austria")
                .latitude(48.2036363f)
                .longitude(16.369240f)
                .approved(true)
                .instructors(List.of(instructors.get(7), instructors.get(8)))
                .profileImage(ProfileImage.builder()
                        .name("aquafit_wellness_center")
                        .path("https://www.schwimmschule-steiner.at/wp-content/uploads/2020/08/Amalienbad-845x684.jpg")
                        .build())
                .galleryImages(List.of(
                        GalleryImage.builder()
                                .name("wellness_center_2")
                                .path("https://www.theamauris.com/_novaimg/galleria/1504540.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("wellness_center_3")
                                .path("https://www.krainerhuette.at/fileadmin/_processed_/2/1/csm_header_hallenbad_9b91cbcf5d.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("wellness_center_4")
                                .path("https://amaymca.org/wp-content/uploads/sites/10/2022/05/Swim-Lessons.jpeg")
                                .build()
                ))
                .studioActivities(List.of(
                        StudioActivity.builder()
                                .name("Aqua Zumba")
                                .description("A lively water-based Zumba class.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0))
                                .duration(50.0f)
                                .price(25.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.WATER_SPORTS))
                                .instructor(instructors.get(7))
                                .profileImage(ProfileImage.builder()
                                        .name("aqua_zumba")
                                        .path("https://d1s9j44aio5gjs.cloudfront.net/2016/09/Aqua_Aerobics_Benefits.jpg")
                                        .build())
                                .capacity(45)
                                .type(ActivityType.WATER_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Swimming Techniques")
                                .description("Improve your swimming strokes with professional training.")
                                .dateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0))
                                .duration(60.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.WATER_SPORTS))
                                .price(30.0f)
                                .instructor(instructors.get(8))
                                .profileImage(ProfileImage.builder()
                                        .name("swimming_techniques")
                                        .path("https://www.schwimmschule-steiner.at/wp-content/uploads/2017/08/schwimmer2-1030x773.jpg")
                                        .build())
                                .capacity(20)
                                .type(ActivityType.WATER_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Aqua Gymnastics")
                                .description("Improve your swimming strokes with professional training.")
                                .dateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0))
                                .duration(60.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.WATER_SPORTS))
                                .price(30.0f)
                                .instructor(instructors.get(8))
                                .profileImage(ProfileImage.builder()
                                        .name("swimming_techniques")
                                        .path("https://www.schwimmschule-steiner.at/wp-content/uploads/2017/08/schwimmer2-1030x773.jpg")
                                        .build())
                                .capacity(20)
                                .type(ActivityType.WATER_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Water Jumping")
                                .description("Improve your swimming strokes with professional training.")
                                .dateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0))
                                .duration(60.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.WATER_SPORTS))
                                .price(30.0f)
                                .instructor(instructors.get(8))
                                .profileImage(ProfileImage.builder()
                                        .name("swimming_techniques")
                                        .path("https://www.schwimmschule-steiner.at/wp-content/uploads/2017/08/schwimmer2-1030x773.jpg")
                                        .build())
                                .capacity(20)
                                .type(ActivityType.WATER_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Snorkeling")
                                .description("Improve your swimming strokes with professional training.")
                                .dateTime(LocalDateTime.now().plusDays(4).withHour(15).withMinute(0))
                                .duration(60.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.WATER_SPORTS))
                                .price(30.0f)
                                .instructor(instructors.get(8))
                                .profileImage(ProfileImage.builder()
                                        .name("swimming_techniques")
                                        .path("https://www.schwimmschule-steiner.at/wp-content/uploads/2017/08/schwimmer2-1030x773.jpg")
                                        .build())
                                .capacity(20)
                                .type(ActivityType.WATER_SPORTS)
                                .build()
                ))
                .studioAdmin(null)
                .build();

        studioRepository.saveAll(Arrays.asList(combatArena, aquaticStudio));
        ApplicationUser studioAdmin1 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password)
                .email("combatarena@email.com")
                .firstName("Combat")
                .lastName("Arena")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Schönbrunner Schloßstraße 47, 1130 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin1);
        combatArena.setStudioAdmin(studioAdmin1);
        studioRepository.save(combatArena);

        ApplicationUser studioAdmin2 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password)
                .email("radina@email.com")
                .firstName("Radina")
                .lastName("Grancharova")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.FEMALE)
                .location("Opernring 2, 1010 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin2);
        aquaticStudio.setStudioAdmin(studioAdmin2);
        studioRepository.save(aquaticStudio);
    }

    private void generateRandomBookings() {
        LOG.info("Generating bookings....");
        List<ApplicationUser> users = applicationUserRepository.findUsersThatAreNotStudioAdminsOrBlocked();
        List<StudioActivity> activities = studioActivityRepository.findAll();

        Random random = new Random();

        for (ApplicationUser user : users) {
            List<StudioActivity> userActivities = new ArrayList<>();

            int activityCount = random.nextInt(5) + 1;

            while (userActivities.size() < activityCount) {

                int studioActivityId = random.nextInt(activities.size());

                StudioActivity randomActivity = activities.get(studioActivityId);

                if (!userActivities.contains(randomActivity) && randomActivity.getApplicationUsers().size() < 0.9 * randomActivity.getCapacity()) {
                    userActivities.add(randomActivity);
                }
            }
            user.setStudioActivities(userActivities);
            applicationUserRepository.save(user);
        }
    }

    private void generateStudioReviews() {
        LOG.info("Generating reviews....");
        List<ApplicationUser> users = applicationUserRepository.findUsersThatAreNotStudioAdminsOrBlocked();
        Random random = new Random();

        for (ApplicationUser user : users) {
            List<Studio> studiosForReview = studioRepository.findStudiosForWhichUserHasBookings(user.getApplicationUserId());
            for (Studio studio : studiosForReview) {
                Review review = Review.builder()
                        .text("My review text")
                        .rating(random.nextInt(5) + 1)
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .studio(studio)
                        .build();
                reviewRepository.save(review);
            }
        }
    }


}
