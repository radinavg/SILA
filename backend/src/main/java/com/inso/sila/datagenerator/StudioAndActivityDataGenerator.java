package com.inso.sila.datagenerator;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Faqs;
import com.inso.sila.entity.GalleryImage;
import com.inso.sila.entity.Instructor;
import com.inso.sila.entity.Membership;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.entity.StudioActivityPreferences;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.enums.Gender;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FaqsRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.repository.ProfileImageRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioActivityPreferencesRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class StudioAndActivityDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final StudioActivityRepository studioActivityRepository;
    private final StudioRepository studioRepository;
    private final ProfileImageRepository profileImageRepository;
    private final FaqsRepository faqsRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final StudioActivityPreferencesRepository preferencesRepository;
    private final ReviewRepository reviewRepository;
    private String password = "password";
    Studio yogaStudio;
    Studio fitnessCenter;
    Studio aquaticStudio;
    Studio sportsArena;
    Studio notApprovedStudio;

    public StudioAndActivityDataGenerator(StudioActivityRepository studioActivityRepository, StudioRepository studioRepository,
                                          ProfileImageRepository profileImageRepository, FaqsRepository faqsRepository,
                                          MembershipRepository membershipRepository, ApplicationUserRepository applicationUserRepository,
                                          StudioActivityPreferencesRepository preferencesRepository, ReviewRepository reviewRepository, PasswordEncoder passwordEncoder) {
        this.studioActivityRepository = studioActivityRepository;
        this.studioRepository = studioRepository;
        this.profileImageRepository = profileImageRepository;
        this.faqsRepository = faqsRepository;
        this.membershipRepository = membershipRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.preferencesRepository = preferencesRepository;
        this.reviewRepository = reviewRepository;
        this.password = passwordEncoder.encode(password);
    }

    private StudioActivityPreferences createDefaultPreferencesForActivityType(ActivityType activityType) {
        StudioActivityPreferences preferences = new StudioActivityPreferences();

        switch (activityType) {
            case YOGA:
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
                break;

            case GROUP_ACTIVITIES:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
                preferences.setSuitableColdClimate(true);
                preferences.setRainCompatibility(true);
                preferences.setWindSuitability(true);
                preferences.setInvolvesUpperBody(true);
                preferences.setInvolvesLowerBody(true);
                preferences.setInvolvesCore(true);
                preferences.setInvolvesFullBody(true);
                preferences.setBeginner(false);
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(5);
                preferences.setGoalStrength(false);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case BALL_SPORTS:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                preferences.setSuitableWarmClimate(true);
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
                preferences.setPhysicalDemandLevel(7);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case OUTDOOR_CLASSES:
                preferences.setIndividual(false);
                preferences.setTeam(true);
                preferences.setWaterBased(false);
                preferences.setIndoor(false);
                preferences.setOutdoor(true);
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
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(6);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(false);
                break;

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
                preferences.setPhysicalDemandLevel(8);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(false);
                preferences.setGoalMentalFocus(true);
                break;

            case WATER_SPORTS:
                preferences.setIndividual(true);
                preferences.setTeam(false);
                preferences.setWaterBased(true);
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
                preferences.setIntermediate(true);
                preferences.setAdvanced(true);
                preferences.setPhysicalDemandLevel(7);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
                preferences.setGoalFlexibility(false);
                preferences.setGoalBalanceCoordination(true);
                preferences.setGoalMentalFocus(false);
                break;

            case FITNESS_CLASSES:
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
                preferences.setBeginner(true);
                preferences.setIntermediate(true);
                preferences.setAdvanced(false);
                preferences.setPhysicalDemandLevel(4);
                preferences.setGoalStrength(true);
                preferences.setGoalEndurance(true);
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

    public void generateStudios() {
        if (!studioRepository.findAll().isEmpty() && !profileImageRepository.findAll().isEmpty() && !studioActivityRepository.findAll().isEmpty()) {
            LOG.info("Studios and activities already generated");
        } else {
            LOG.info("Generating studios and activities...");
            generateStudiosData();
            generateAdditionalStudios();
            generateGroupSportsActivities();
            generateWaterSportsActivities();
            generateYogaActivities();
            generateCombatSportsActivities();
            generateFitnessActivities();
            addFaqsAndMemberships();
            createStudioAdmin();
            generateRandomBookings();
            generateStudioReviews();
        }

    }

    private void generateStudiosData() {

        List<Instructor> instructors = getInstructors();

        yogaStudio = Studio.builder()
                .name("Tranquility Yoga Center")
                .description("A serene space dedicated to enhancing your mind and body connection through yoga.")
                .location("Stephansplatz 3, 1010 Vienna, Austria")
                .latitude(48.2082063f)
                .longitude(16.37281436f)
                .approved(true)
                .instructors(List.of(instructors.get(0), instructors.get(1)))
                .profileImage(ProfileImage.builder()
                        .name("tranquility_yoga_center")
                        .path("https://www.traineressentials.com/wp-content/uploads/2020/12/Colourful-Personal-Training-Studio-Design.jpg")
                        .build())
                .galleryImages(List.of(
                        GalleryImage.builder()
                                .name("yoga_2")
                                .path("https://mir-s3-cdn-cf.behance.net/project_modules/1400/3c9c68180540055.650c4944a529c.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("yoga_3")
                                .path("https://mojoboutique.com/cdn/shop/articles/japandi_for_yoga_studio_1344x.jpg?v=1704744991")
                                .build(),
                        GalleryImage.builder()
                                .name("yoga_4")
                                .path("https://media.istockphoto.com/id/1497863129/photo/white-and-wooden-yoga-studio-corner-with-mats-and-fitballs.jpg?s=612x612&w=0&k=20&c=IX6qYmQKc5O2cCau0CCdx6CBQLA6naLLIWczSYCyqsw=")
                                .build()
                ))
                .studioActivities(List.of(
                        StudioActivity.builder()
                                .name("Sunrise Yoga")
                                .description("A calming morning yoga session.")
                                .dateTime(LocalDateTime.now().plusDays(1).withHour(6).withMinute(30))
                                .duration(60.0f)
                                .price(15.0f)
                                .instructor(instructors.get(0))
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.YOGA))
                                .profileImage(ProfileImage.builder()
                                        .name("sunrise_yoga")
                                        .path("https://files.eversports.com/9a255733-edd7-44d4-99a7-69be4f85cb7d/2o4a6099-bearbeitet_webjpg-medium.webp")
                                        .build())
                                .capacity(25)
                                .type(ActivityType.YOGA)
                                .build(),
                        StudioActivity.builder()
                                .name("Power Flow Yoga")
                                .description("An energetic yoga class to build strength.")
                                .dateTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0))
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.YOGA))
                                .duration(75.0f)
                                .price(18.0f)
                                .instructor(instructors.get(1))
                                .profileImage(ProfileImage.builder()
                                        .name("power_flow_yoga")
                                        .path("https://ai.flux-image.com/flux/3a6e23d8-481a-4f13-a100-a1a9a6a8395b.jpg")
                                        .build())
                                .type(ActivityType.YOGA)
                                .capacity(20)
                                .build()
                ))
                .studioAdmin(null)
                .build();

        fitnessCenter = Studio.builder()
                .name("Dynamic Fitness Hub")
                .description("An all-in-one fitness center catering to group workouts and personal training.")
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
                                .name("HIIT Blast")
                                .description("A high-intensity interval training session to burn calories.")
                                .dateTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0))
                                .duration(45.0f)
                                .price(20.0f)
                                .instructor(instructors.get(2))
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.FITNESS_CLASSES))
                                .profileImage(ProfileImage.builder()
                                        .name("hiit_blast")
                                        .path("https://media.self.com/photos/5c10255b2f04d8625a2fbb64/master/pass/women-with-dumbbells.jpg")
                                        .build())
                                .capacity(15)
                                .type(ActivityType.FITNESS_CLASSES)
                                .build(),
                        StudioActivity.builder()
                                .name("Group Strength Training")
                                .description("A collaborative strength workout with expert guidance.")
                                .dateTime(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0))
                                .duration(60.0f)
                                .price(22.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                                .instructor(instructors.get(3))
                                .profileImage(ProfileImage.builder()
                                        .name("group_strength_training")
                                        .path("https://lifthousefitness.com/wp-content/uploads/2021/02/shutterstock_1049628212-1024x683.png")
                                        .build())
                                .capacity(30)
                                .type(ActivityType.GROUP_ACTIVITIES)
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
                                .build()

                ))
                .studioAdmin(null)
                .build();

        sportsArena = Studio.builder()
                .name("Victory Sports Arena")
                .description("A hub for ball sports enthusiasts, offering courts and coaching.")
                .location("Prinz-Eugen-Straße 27, 1030 Vienna, Austria")
                .latitude(48.19155f)
                .longitude(16.380876f)
                .approved(true)
                .instructors(List.of(instructors.get(4), instructors.get(5), instructors.get(6)))
                .profileImage(ProfileImage.builder()
                        .name("victory_sports_arena")
                        .path("https://i.pinimg.com/550x/66/19/af/6619afddaf9c3b6d66cda7948c1e9692.jpg")
                        .build())
                .galleryImages(List.of(
                        GalleryImage.builder()
                                .name("sports_arena_2")
                                .path("https://robbreport.com/wp-content/uploads/2022/03/200803_EJ_waterline_square-044944_HIGH_RES.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("sports_arena_3")
                                .path("https://cdn11.bigcommerce.com/s-cf8ys9ikz8/images/stencil/1280x1280/m/indoor-nets-category__80096.original_category.original.jpg")
                                .build(),
                        GalleryImage.builder()
                                .name("sports_arena_4")
                                .path("https://b2370844.smushcdn.com/2370844/resources/uploads/2021/04/AirTrack-Factory-Apex-AirCourt.jpg?lossy=1&strip=1&webp=1")
                                .build()
                ))
                .studioActivities(List.of(
                        StudioActivity.builder()
                                .name("Basketball Skills Workshop")
                                .description("Hone your basketball techniques with professional coaches.")
                                .dateTime(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
                                .duration(90.0f)
                                .price(25.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.BALL_SPORTS))
                                .instructor(instructors.get(4))
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
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.BALL_SPORTS))
                                .instructor(instructors.get(5))
                                .profileImage(ProfileImage.builder()
                                        .name("volleyball_practice")
                                        .path("https://media.istockphoto.com/id/489699302/photo/women-spiking-and-blocking-a-volleyball.jpg?s=612x612&w=0&k=20&c=eSnMWORi8fjVZVNwmmUSvIWRVDgEQRfmVVgVD2uFo0E=")
                                        .build())
                                .capacity(30)
                                .type(ActivityType.BALL_SPORTS)
                                .build()
                ))
                .studioAdmin(null)
                .build();


        aquaticStudio = Studio.builder()
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
                                .build()
                ))
                .studioAdmin(null)
                .build();

        notApprovedStudio = Studio.builder()
                .name("New Studio In Town")
                .description("Discover our brand new, state-of-the-art pilates studio in the heart of town.")
                .location("Praterstraße 9, 1020 Vienna, Austria")
                .latitude(48.2130503f)
                .longitude(16.380651f)
                .approved(false)
                .profileImage(ProfileImage.builder()
                        .name("new-studio")
                        .path("https://www.baltimoremagazine.com/wp-content/uploads/2022/03/FORM-1200x800.jpg")
                        .build())
                .studioAdmin(null)
                .build();

        studioRepository.saveAll(Arrays.asList(yogaStudio, fitnessCenter, sportsArena, aquaticStudio, notApprovedStudio));
    }

    private void generateAdditionalStudios() {
        List<String> studioNames = Arrays.asList(
                "Zen Retreat Studio", "Peak Performance Gym", "Harmony Wellness Hub", "Infinite Motion Studio",
                "Vitality Fitness Center", "Energize Wellness Studio", "Balance Yoga Place", "Strength Arena",
                "Core Power Studio", "Elevate Fitness Zone", "Pulse Health Hub", "Momentum Fitness Center",
                "Aspire Studio", "Synergy Health Club", "Renewal Studio", "Revive Wellness Place",
                "Excel Fitness Arena", "Ignite Studio", "Ascend Wellness Zone", "Dynamic Core Hub",
                "Trailblazer Studio", "Powerhouse Fitness", "Prime Studio", "Fortitude Wellness Place",
                "Velocity Studio", "Serenity Flow Studio", "Resilience Gym", "Mindful Movement Center",
                "Primal Strength Arena", "Om Balance Studio", "Recharge Wellness Club", "Thrive Training Hub",
                "Zenith Performance Studio", "Uplift Yoga Center", "Hyperfit Gym", "Invigorate Wellness",
                "Unstoppable Fitness", "Summit Strength Studio", "Synergy Flow Studio", "Tranquil Core Studio",
                "Peak Vitality Center", "Elevation Training Club", "Vortex Fitness Hub", "Empower Wellness Zone",
                "Endurance Elite Studio", "Momentum Powerhouse", "Transform Wellness Studio", "Titan Strength Gym",
                "Rejuvenate Yoga & Wellness", "Oasis Fitness Retreat", "Fusion Training Center"
        );

        for (int i = 0; i < studioNames.size(); i++) {
            float latitude = 48.2081F;
            float longitude = 16.3713F;
            float random = (float) (Math.random() * 0.1F - 0.05F);
            ApplicationUser studioAdmin1 = ApplicationUser.builder()
                    .isAdmin(Boolean.FALSE)
                    .isStudioAdmin(Boolean.TRUE)
                    .password(password)
                    .email("studio_admin" + i + "@gmail.com")
                    .firstName(studioNames.get(i))
                    .lastName("Studio Admin")
                    .isLocked(Boolean.FALSE)
                    .loginAttempts(0)
                    .gender(Gender.MALE)
                    .location("Studio Location " +  studioNames.get(i))
                    .latitude(latitude + random)
                    .longitude(longitude + random)
                    .build();

            applicationUserRepository.save(studioAdmin1);

            Studio studio = Studio.builder()
                    .name(studioNames.get(i))
                    .description("A unique fitness and wellness space tailored for personal growth and community.")
                    .location("Studio Street " + (i + 1) + ", Vienna")
                    .latitude(latitude + random)
                    .longitude(longitude + random)
                    .approved(true)
                    .studioAdmin(studioAdmin1)
                    .profileImage(ProfileImage.builder()
                            .name("studio_profile_image")
                            .path("https://i.redd.it/cha71w40li071.jpg")
                            .build())
                    .build();

            studioRepository.save(studio);
        }
    }

    private void generateGroupSportsActivities() {
        List<Studio> studios = studioRepository.findByApprovedTrue();

        if (studios.isEmpty()) {
            LOG.warn("No studios available to assign activities.");
            return;
        }

        List<String> activityNames = Arrays.asList(
                "Ultimate Frisbee", "Team Handball", "Volleyball Practice", "Basketball Scrimmage",
                "Soccer Skills Training", "Dodgeball Madness", "Floor Hockey Basics", "Cricket Fundamentals",
                "Rugby Tactics", "Badminton Doubles", "Table Tennis Practice", "Pickleball Challenge",
                "Water Polo Introduction", "Beach Volleyball Skills", "Softball Basics", "Football Drills",
                "Lacrosse Practice", "Team Relay Challenge", "Korfball Fundamentals", "Futsal Training"
        );

        int studioIndex = 0;

        for (int i = 0; i < activityNames.size(); i++) {
            Studio currentStudio = studios.get(studioIndex);

            StudioActivity activity = StudioActivity.builder()
                    .name(activityNames.get(i))
                    .description("An engaging group sport activity to enhance team skills and have fun.")
                    .dateTime(LocalDateTime.now().plusDays((i % 5) + 1).withHour(10 + (i % 6)).withMinute(0))
                    .duration(90.0f)
                    .price(20.0f + (i % 10))
                    .capacity(15 + (i % 10))
                    .type(ActivityType.GROUP_ACTIVITIES)
                    .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                    .profileImage(ProfileImage.builder()
                            .name("new-activity" + activityNames.get(i))
                            .path("https://cdn.dribbble.com/userupload/16568411/file/original-c29610cd7ab3d04897250455390d0fb0.png?resize=1024x768&vertical=center")
                            .build())
                    .build();

            currentStudio.getStudioActivities().add(activity);

            studioRepository.save(currentStudio);

            studioIndex = (studioIndex + 1) % studios.size();
        }
    }

    private void generateWaterSportsActivities() {
        List<Studio> studios = studioRepository.findByApprovedTrue();

        if (studios.isEmpty()) {
            LOG.warn("No studios available to assign activities.");
            return;
        }

        List<String> waterSportsActivities = Arrays.asList(
                "Kayaking Adventures", "Paddleboard Yoga", "Surfing Basics", "Snorkeling Exploration",
                "Scuba Diving Intro", "Canoe Racing", "Rowing Endurance", "Wakeboarding Skills",
                "Windsurfing Techniques", "Kitesurfing Challenge", "Open Water Swimming", "Synchronized Swimming",
                "Water Polo Scrimmage", "Hydro Fitness", "Jet Ski Maneuvers", "Freediving Essentials",
                "Rafting Rapids", "Aqua Zumba", "Underwater Hockey", "Sailing Fundamentals"
        );

        int studioIndex = 0;

        for (int i = 0; i < waterSportsActivities.size(); i++) {
            Studio currentStudio = studios.get(studioIndex);

            StudioActivity activity = StudioActivity.builder()
                    .name(waterSportsActivities.get(i))
                    .description("Make a splash and ride the waves with adrenaline-pumping water sports.")
                    .dateTime(LocalDateTime.now().plusDays((i % 5) + 1).withHour(10 + (i % 6)).withMinute(0))
                    .duration(90.0f)
                    .price(20.0f + (i % 10))
                    .capacity(25 + (i % 10))
                    .type(ActivityType.FITNESS_CLASSES)
                    .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                    .profileImage(ProfileImage.builder()
                            .name("new-activity" + waterSportsActivities.get(i))
                            .path("https://cdn.dribbble.com/userupload/16568411/file/original-c29610cd7ab3d04897250455390d0fb0.png?resize=1024x768&vertical=center")
                            .build())
                    .build();

            currentStudio.getStudioActivities().add(activity);

            studioRepository.save(currentStudio);

            studioIndex = (studioIndex + 1) % studios.size();
        }
    }

    private void generateYogaActivities() {
        List<Studio> studios = studioRepository.findByApprovedTrue();

        if (studios.isEmpty()) {
            LOG.warn("No studios available to assign activities.");
            return;
        }

        List<String> yogaActivities = Arrays.asList(
                "Vinyasa Flow", "Power Yoga", "Hatha Yoga Basics", "Ashtanga Practice",
                "Yin Yoga Relaxation", "Restorative Yoga", "Bikram Hot Yoga", "Chair Yoga",
                "Kundalini Awakening", "Prenatal Yoga", "Meditative Yoga", "Yoga for Flexibility",
                "Yoga for Strength", "Yoga for Balance", "Sun Salutations", "Mindful Yoga",
                "Aerial Yoga", "Yoga Nidra", "Core Yoga Workout", "Yoga and Breathwork"
        );

        int studioIndex = 0;

        for (int i = 0; i < yogaActivities.size(); i++) {
            Studio currentStudio = studios.get(studioIndex);

            StudioActivity activity = StudioActivity.builder()
                    .name(yogaActivities.get(i))
                    .description("Find your flow, embrace mindfulness and enhance flexibility, strength, and inner peace.")
                    .dateTime(LocalDateTime.now().plusDays((i % 5) + 1).withHour(10 + (i % 6)).withMinute(0))
                    .duration(90.0f)
                    .price(20.0f + (i % 10))
                    .capacity(25 + (i % 10))
                    .type(ActivityType.FITNESS_CLASSES)
                    .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                    .profileImage(ProfileImage.builder()
                            .name("new-activity" + yogaActivities.get(i))
                            .path("https://cdn.dribbble.com/userupload/16568411/file/original-c29610cd7ab3d04897250455390d0fb0.png?resize=1024x768&vertical=center")
                            .build())
                    .build();

            currentStudio.getStudioActivities().add(activity);

            studioRepository.save(currentStudio);

            studioIndex = (studioIndex + 1) % studios.size();
        }
    }

    private void generateCombatSportsActivities() {
        List<Studio> studios = studioRepository.findByApprovedTrue();

        if (studios.isEmpty()) {
            LOG.warn("No studios available to assign activities.");
            return;
        }

        List<String> combatSportsActivities = Arrays.asList(
                "Boxing Fundamentals", "Muay Thai Drills", "Brazilian Jiu-Jitsu", "Wrestling Techniques",
                "Karate Kata Practice", "Taekwondo Kicks", "Judo Throws", "Kickboxing Training",
                "Mixed Martial Arts", "Self-Defense Tactics", "Fencing Basics", "Sword Fighting Skills",
                "Capoeira Movements", "Kendo Sparring", "Sambo Grappling", "Savate Footwork",
                "Sumo Wrestling", "Combat Fitness", "Jiu-Jitsu Rolling", "Stick Fighting"
        );

        int studioIndex = 0;

        for (int i = 0; i < combatSportsActivities.size(); i++) {
            Studio currentStudio = studios.get(studioIndex);

            StudioActivity activity = StudioActivity.builder()
                    .name(combatSportsActivities.get(i))
                    .description("Unleash your power and master precision in combat sports!")
                    .dateTime(LocalDateTime.now().plusDays((i % 5) + 1).withHour(10 + (i % 6)).withMinute(0))
                    .duration(90.0f)
                    .price(20.0f + (i % 10))
                    .capacity(25 + (i % 10))
                    .type(ActivityType.FITNESS_CLASSES)
                    .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                    .profileImage(ProfileImage.builder()
                            .name("new-activity" + combatSportsActivities.get(i))
                            .path("https://cdn.dribbble.com/userupload/16568411/file/original-c29610cd7ab3d04897250455390d0fb0.png?resize=1024x768&vertical=center")
                            .build())
                    .build();

            currentStudio.getStudioActivities().add(activity);

            studioRepository.save(currentStudio);

            studioIndex = (studioIndex + 1) % studios.size();
        }
    }

    private void generateFitnessActivities() {
        List<Studio> studios = studioRepository.findByApprovedTrue();

        if (studios.isEmpty()) {
            LOG.warn("No studios available to assign activities.");
            return;
        }

        List<String> fitnessClasses = Arrays.asList(
                "HIIT Cardio Blast", "Strength Training", "Core Conditioning", "Full-Body Workout",
                "Circuit Training", "Bootcamp Challenge", "Functional Fitness", "Plyometrics Drills",
                "Bodyweight Workout", "Cardio Kickboxing", "Dance Fitness Party", "Indoor Cycling",
                "Step Aerobics", "CrossFit Fundamentals", "TRX Suspension Training", "Barre Strength",
                "Pilates Flow", "Tabata Intervals", "Kettlebell Training", "Mobility and Recovery"
        );

        int studioIndex = 0;

        for (int i = 0; i < fitnessClasses.size(); i++) {
            Studio currentStudio = studios.get(studioIndex);

            StudioActivity activity = StudioActivity.builder()
                    .name(fitnessClasses.get(i))
                    .description("Break a sweat and push your limits in a high-energy fitness class.")
                    .dateTime(LocalDateTime.now().plusDays((i % 5) + 1).withHour(10 + (i % 6)).withMinute(0))
                    .duration(90.0f)
                    .price(20.0f + (i % 10))
                    .capacity(25 + (i % 10))
                    .type(ActivityType.FITNESS_CLASSES)
                    .preferences(createDefaultPreferencesForActivityType(ActivityType.GROUP_ACTIVITIES))
                    .profileImage(ProfileImage.builder()
                            .name("new-activity" + fitnessClasses.get(i))
                            .path("https://cdn.dribbble.com/userupload/16568411/file/original-c29610cd7ab3d04897250455390d0fb0.png?resize=1024x768&vertical=center")
                            .build())
                    .build();

            currentStudio.getStudioActivities().add(activity);

            studioRepository.save(currentStudio);

            studioIndex = (studioIndex + 1) % studios.size();
        }
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

    private void addFaqsAndMemberships() {
        // Adding FAQs for each studio
        List<Faqs> yogaFaqs = List.of(
                Faqs.builder()
                        .question("What should I bring to my first class?")
                        .answer("Comfortable clothing, a yoga mat, and a water bottle are recommended.")
                        .studio(yogaStudio)
                        .build(),
                Faqs.builder()
                        .question("Are there beginner-friendly classes?")
                        .answer("Yes, we offer classes tailored to all skill levels.")
                        .studio(yogaStudio)
                        .build(),
                Faqs.builder()
                        .question("Do you offer private yoga sessions?")
                        .answer("Yes, private sessions can be arranged with our instructors.")
                        .studio(yogaStudio)
                        .build()
        );

        List<Faqs> fitnessFaqs = List.of(
                Faqs.builder()
                        .question("What is your cancellation policy?")
                        .answer("You can cancel up to 24 hours before the class starts.")
                        .studio(fitnessCenter)
                        .build(),
                Faqs.builder()
                        .question("Do I need to book classes in advance?")
                        .answer("Yes, it’s recommended to book classes at least a day in advance.")
                        .studio(fitnessCenter)
                        .build(),
                Faqs.builder()
                        .question("Are lockers available?")
                        .answer("Yes, lockers are available free of charge for all members.")
                        .studio(fitnessCenter)
                        .build()
        );

        List<Faqs> sportsFaqs = List.of(
                Faqs.builder()
                        .question("Do you offer group discounts?")
                        .answer("Yes, discounts are available for groups of 5 or more.")
                        .studio(sportsArena)
                        .build(),
                Faqs.builder()
                        .question("Can I rent equipment at the arena?")
                        .answer("Yes, we provide rental equipment for basketball, volleyball, and more.")
                        .studio(sportsArena)
                        .build(),
                Faqs.builder()
                        .question("Is there parking available?")
                        .answer("Yes, ample parking is available on-site.")
                        .studio(sportsArena)
                        .build()
        );

        List<Faqs> aquaticFaqs = List.of(
                Faqs.builder()
                        .question("Is there an age limit for water activities?")
                        .answer("Participants must be at least 12 years old for Aqua Zumba.")
                        .studio(aquaticStudio)
                        .build(),
                Faqs.builder()
                        .question("Do you offer swimming lessons for beginners?")
                        .answer("Yes, we have beginner-friendly swimming lessons available.")
                        .studio(aquaticStudio)
                        .build(),
                Faqs.builder()
                        .question("Are there family discounts for memberships?")
                        .answer("Yes, discounted family packages are available for AquaFit memberships.")
                        .studio(aquaticStudio)
                        .build()
        );

        // Save FAQs to repository
        faqsRepository.saveAll(yogaFaqs);
        faqsRepository.saveAll(fitnessFaqs);
        faqsRepository.saveAll(sportsFaqs);
        faqsRepository.saveAll(aquaticFaqs);

        // Adding Memberships for each studio
        List<Membership> yogaMemberships = List.of(
                Membership.builder()
                        .name("Monthly Yoga Pass")
                        .minDuration(1)
                        .price(50.0f)
                        .studio(yogaStudio)
                        .build(),
                Membership.builder()
                        .name("Quarterly Yoga Pass")
                        .minDuration(2)
                        .price(135.0f)
                        .studio(yogaStudio)
                        .build(),
                Membership.builder()
                        .name("Annual Yoga Membership")
                        .minDuration(12)
                        .price(480.0f)
                        .studio(yogaStudio)
                        .build()
        );

        List<Membership> fitnessMemberships = List.of(
                Membership.builder()
                        .name("Dynamic Fitness Monthly")
                        .minDuration(1)
                        .price(60.0f)
                        .studio(fitnessCenter)
                        .build(),
                Membership.builder()
                        .name("Dynamic Fitness Quarterly")
                        .minDuration(2)
                        .price(165.0f)
                        .studio(fitnessCenter)
                        .build(),
                Membership.builder()
                        .name("Dynamic Fitness Annual")
                        .minDuration(12)
                        .price(600.0f)
                        .studio(fitnessCenter)
                        .build()
        );

        List<Membership> sportsMemberships = List.of(
                Membership.builder()
                        .name("Annual Sports Membership")
                        .minDuration(12)
                        .price(200.0f)
                        .studio(sportsArena)
                        .build(),
                Membership.builder()
                        .name("Quarterly Sports Membership")
                        .minDuration(3)
                        .price(60.0f)
                        .studio(sportsArena)
                        .build(),
                Membership.builder()
                        .name("Monthly Sports Pass")
                        .minDuration(1)
                        .price(25.0f)
                        .studio(sportsArena)
                        .build()
        );

        List<Membership> aquaticMemberships = List.of(
                Membership.builder()
                        .name("AquaFit Premium")
                        .minDuration(1)
                        .price(75.0f)
                        .studio(aquaticStudio)
                        .build(),
                Membership.builder()
                        .name("Quarterly AquaFit Membership")
                        .minDuration(2)
                        .price(210.0f)
                        .studio(aquaticStudio)
                        .build(),
                Membership.builder()
                        .name("Annual Aqua Wellness Pass")
                        .minDuration(12)
                        .price(720.0f)
                        .studio(aquaticStudio)
                        .build()
        );

        // Save Memberships to repository
        membershipRepository.saveAll(yogaMemberships);
        membershipRepository.saveAll(fitnessMemberships);
        membershipRepository.saveAll(sportsMemberships);
        membershipRepository.saveAll(aquaticMemberships);

        // Update Studios with FAQs and Memberships
        yogaStudio.setFaqs(yogaFaqs);
        yogaStudio.setMemberships(yogaMemberships);
        fitnessCenter.setFaqs(fitnessFaqs);
        fitnessCenter.setMemberships(fitnessMemberships);
        sportsArena.setFaqs(sportsFaqs);
        sportsArena.setMemberships(sportsMemberships);
        aquaticStudio.setFaqs(aquaticFaqs);
        aquaticStudio.setMemberships(aquaticMemberships);

        // Save updated Studios
        studioRepository.saveAll(List.of(yogaStudio, fitnessCenter, sportsArena, aquaticStudio));
    }

    private void createStudioAdmin() {

        // Create a new ApplicationUser using the builder pattern
        ApplicationUser studioAdmin1 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password) // Use the encrypted password
                .email("ivan@email.com")
                .firstName("Ivan")
                .lastName("Andreev")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Rathausplatz 1, Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin1);
        yogaStudio.setStudioAdmin(studioAdmin1);
        studioRepository.save(yogaStudio);

        // Create a new ApplicationUser using the builder pattern
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
                .location("Kegelgasse 34, 1030 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin2);
        fitnessCenter.setStudioAdmin(studioAdmin2);
        studioRepository.save(fitnessCenter);


        // Create a new ApplicationUser using the builder pattern
        ApplicationUser studioAdmin3 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password)
                .email("sports@email.com")
                .firstName("Sports")
                .lastName("Arena")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Mariahilfer Straße 123, 1060 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin3);
        aquaticStudio.setStudioAdmin(studioAdmin3);
        studioRepository.save(aquaticStudio);

        // Create a new ApplicationUser using the builder pattern
        ApplicationUser studioAdmin4 = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password)
                .email("water@email.com")
                .firstName("Water")
                .lastName("Sports")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.MALE)
                .location("Karlsplatz 1, 1040 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin4);
        sportsArena.setStudioAdmin(studioAdmin4);
        studioRepository.save(sportsArena);

        // Create a new ApplicationUser using the builder pattern
        ApplicationUser studioAdmin = ApplicationUser.builder()
                .isAdmin(Boolean.FALSE)
                .isStudioAdmin(Boolean.TRUE)
                .password(password)
                .email("new@email.com")
                .firstName("New")
                .lastName("Studio")
                .isLocked(Boolean.FALSE)
                .loginAttempts(0)
                .gender(Gender.FEMALE)
                .location("Alser Straße 4, 1090 Vienna, Austria")
                .build();

        applicationUserRepository.save(studioAdmin);
        notApprovedStudio.setStudioAdmin(studioAdmin);
        studioRepository.save(notApprovedStudio);
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
