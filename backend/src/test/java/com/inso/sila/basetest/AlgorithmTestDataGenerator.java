package com.inso.sila.basetest;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.GalleryImage;
import com.inso.sila.entity.Instructor;
import com.inso.sila.entity.ProfileImage;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.entity.StudioActivityPreferences;
import com.inso.sila.enums.ActivityType;
import com.inso.sila.repository.ActivityInvitationRepository;
import com.inso.sila.repository.ActivityTypeAttributesRepository;
import com.inso.sila.repository.ApplicationUserPreferencesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FavouriteStudiosRepository;
import com.inso.sila.repository.FriendRequestRepository;
import com.inso.sila.repository.ProfileImageRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioActivityPreferencesRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Profile("test")
@Rollback(false)
public class AlgorithmTestDataGenerator {
    @Autowired
    ApplicationUserRepository applicationUserRepository;
    @Autowired
    StudioActivityRepository studioActivityRepository;
    @Autowired
    ApplicationUserPreferencesRepository applicationUserPreferencesRepository;
    @Autowired
    StudioActivityPreferencesRepository studioActivityPreferencesRepository;
    @Autowired
    StudioRepository studioRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private FavouriteStudiosRepository favouriteStudiosRepository;
    @Autowired
    FriendRequestRepository friendRequestRepository;
    @Autowired
    ActivityInvitationRepository activityInvitationRepository;
    @Autowired
    ActivityTypeAttributesRepository activityTypeAttributesRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    BookingsGenerator bookingsGenerator;
    @Autowired
    ProfileImageRepository profileImageRepository;


    private static final List<String> FIRST_NAMES = Arrays.asList(
            "John", "Jane", "Alex", "Emma", "Michael", "Sarah", "David", "Olivia", "James", "Sophia",
            "Daniel", "Mia", "William", "Isabella", "Benjamin", "Charlotte", "Matthew", "Amelia", "Ethan", "Grace"
    );
    private static final List<String> LAST_NAMES = Arrays.asList(
            "Doe", "Smith", "Brown", "Johnson", "Williams", "Jones", "Davis", "Miller", "Wilson", "Moore",
            "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez"
    );
    private static final List<String> LOCATIONS = Arrays.asList(
            "Innere Stadt", "Leopoldstadt", "Landstraße", "Wieden", "Margareten", "Mariahilf", "Rudolfsheim-Fünfhaus",
            "Penzing", "Ottakring", "Hernals", "Favoriten", "Simmering", "Meidling", "Hietzing", "Fünfhaus",
            "Döbling", "Brigittenau", "Floridsdorf", "Liesing", "Alsergrund", "Neubau", "Rudolfsheim", "Schwedenplatz",
            "Stephansplatz", "Prater", "Schönbrunn", "Kahlenberg", "Praterstern", "Naschmarkt", "Donauinsel",
            "Wienerwald", "Stadtpark", "Donaukanal"
    );

    private static final List<String> ACTIVITY_NAMES = Arrays.asList(
            "Yoga", "Pilates", "CrossFit", "Zumba", "Swimming", "Cycling", "Running", "Hiking", "Boxing", "Martial Arts"
    );
    private static final List<String> DESCRIPTIONS = Arrays.asList(
            "A relaxing and rejuvenating class focused on flexibility.",
            "A high-energy workout that combines dance and aerobic movements.",
            "A strength and conditioning workout combining cardio and strength exercises.",
            "A low-impact workout that targets core strength and flexibility.",
            "An endurance workout that improves cardiovascular health.",
            "A fun and challenging outdoor workout for all levels.",
            "A full-body workout that improves stamina and strength.",
            "A team-based activity to improve coordination and strength.",
            "A fast-paced, intense workout that focuses on strength and conditioning.",
            "A workout focused on agility, coordination, and strength."
    );
    private static final List<ActivityType> ACTIVITY_TYPES = Arrays.asList(
            ActivityType.YOGA, ActivityType.BALL_SPORTS, ActivityType.GROUP_ACTIVITIES, ActivityType.WATER_SPORTS, ActivityType.COMBAT_SPORTS, ActivityType.FITNESS_CLASSES, ActivityType.OUTDOOR_CLASSES
    );


    @Transactional
    @Rollback(false)
    public void cleanTestData() {
        reviewRepository.deleteAll();
        friendRequestRepository.deleteAll();
        activityInvitationRepository.deleteAll();
        favouriteStudiosRepository.deleteAll();
        activityTypeAttributesRepository.deleteAll();
        studioActivityPreferencesRepository.deleteAll();
        applicationUserPreferencesRepository.deleteAll();
        studioActivityRepository.deleteAll();
        studioRepository.deleteAll();
        applicationUserRepository.deleteAll();
    }


    public void generateAlgorithmTestData(int userCount, int activityCount) {
        generateUsers(userCount);
        generateStudios();
        bookingsGenerator.generateRandomBookings();
        bookingsGenerator.generateStudioReviews();

    }

    protected void generateUsers(int count) {
        List<ApplicationUser> users = new ArrayList<>();
        int groupSize = count / 5; // Divide users into 5 logical groups.


        // Generate users for each group
        List<List<ApplicationUser>> groups = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            groups.add(new ArrayList<>());
        }

        // Parallel user generation for each group
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<List<ApplicationUser>>> futures = new ArrayList<>();

        futures.add(executorService.submit(() -> generateGroupUsers(groups.getFirst(), "Outdoor", groupSize)));
        futures.add(executorService.submit(() -> generateGroupUsers(groups.get(1), "Indoor", groupSize)));
        futures.add(executorService.submit(() -> generateGroupUsers(groups.get(2), "Both", groupSize)));
        futures.add(executorService.submit(() -> generateGroupUsers(groups.get(3), "Both", groupSize)));
        futures.add(executorService.submit(() -> generateGroupUsers(groups.get(4), "Both", count - 4 * groupSize)));


        try {
            for (Future<List<ApplicationUser>> future : futures) {
                users.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }

        // Assign friendships


        //assignFriendships(groups);
    }

    private List<ApplicationUser> generateGroupUsers(List<ApplicationUser> group, String activitySetting, int groupSize) {
        List<ApplicationUser> users = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
            users.add(createUserWithPreferences(
                    randomInt(1, 10),                               // physicalDemandLevel
                    randomElement(Arrays.asList("Warm", "Cold")),   // preferredClimate
                    randomElement(Arrays.asList("Beginner", "Intermediate", "Advanced")), // skillLevel
                    randomBoolean(),                                // prefersTeam
                    randomBoolean(),                                // focusLowerBody
                    randomBoolean(),                                // focusCore
                    randomBoolean(),                                // focusFullBody
                    randomElement(Arrays.asList("Strength", "Endurance", "Flexibility", "BalanceCoordination", "MentalFocus")), // primaryGoal
                    activitySetting.equals("Indoor"),               // isIndoor
                    activitySetting.equals("Outdoor"),              // isOutdoor
                    activitySetting.equals("Both")                  // isBothIndoorAndOutdoor
            ));
        }
        group.addAll(users);
        return users;
    }


    private void assignFriendships(List<List<ApplicationUser>> groups) {
        for (List<ApplicationUser> group : groups) {
            for (ApplicationUser user : group) {
                int sameGroupFriends = randomInt(0, 0);
                for (int i = 0; i < sameGroupFriends; i++) {
                    ApplicationUser friend = randomElement(group);
                    if (!user.getFriends().contains(friend) && !friend.equals(user)) {
                        user.getFriends().add(friend);
                        friend.getFriends().add(user);
                        try {
                            applicationUserRepository.save(user);
                            applicationUserRepository.save(friend);
                        }
                        catch (Exception e){
                            continue;
                        }
                    }
                }
            }
        }
    }

    private ApplicationUser createUserWithPreferences(
            int physicalDemandLevel,
            String preferredClimate, String skillLevel, boolean prefersTeam,
            boolean focusLowerBody, boolean focusCore, boolean focusFullBody,
            String primaryGoal,
            boolean isIndoor, boolean isOutdoor, boolean isBothIndoorAndOutdoor

    ) {
        float latitude = 48.2081F;
        float longitude = 16.3713F;
        float random = (float) (Math.random() * 0.1F - 0.05F);
        ApplicationUser user = new ApplicationUser();
        user.setFirstName(randomElement(FIRST_NAMES));
        user.setLastName(randomElement(LAST_NAMES));
        user.setEmail(user.getFirstName().toLowerCase() + "." + user.getLastName().toLowerCase() +
                "." + UUID.randomUUID() + "@example.com");
        user.setFriends(new HashSet<>());
        user.setPassword(passwordEncoder.encode("password"));
        user.setLocation("Street 123" +  ", Vienna");
        user.setLatitude(latitude + random);
        user.setLongitude(longitude + random);
        ApplicationUserPreferences preferences = new ApplicationUserPreferences();
        preferences.setPrefersIndoor(isIndoor);
        preferences.setPrefersOutdoor(isOutdoor);
        preferences.setPrefersBothIndoorAndOutdoor(isBothIndoorAndOutdoor);
        preferences.setPhysicalDemandLevel(physicalDemandLevel);
        preferences.setPrefersWarmClimate(preferredClimate.equals("Warm"));
        preferences.setPrefersColdClimate(preferredClimate.equals("Cold"));
        preferences.setBeginner(skillLevel.equals("Beginner"));
        preferences.setIntermediate(skillLevel.equals("Intermediate"));
        preferences.setAdvanced(skillLevel.equals("Advanced"));
        preferences.setPrefersIndividual(true);
        preferences.setPrefersTeam(prefersTeam);
        preferences.setPrefersWaterBased(true);
        preferences.setFocusUpperBody(true);
        preferences.setFocusLowerBody(focusLowerBody);
        preferences.setFocusCore(focusCore);
        preferences.setFocusFullBody(focusFullBody);
        preferences.setGoalStrength(true);
        preferences.setGoalEndurance(primaryGoal.equals("Endurance"));
        preferences.setGoalFlexibility(primaryGoal.equals("Flexibility"));
        preferences.setGoalBalanceCoordination(primaryGoal.equals("Balance and Coordination"));
        preferences.setGoalMentalFocus(primaryGoal.equals("Mental Focus"));
        preferences.setUser(user);

        user.setPreferences(preferences);

        return applicationUserRepository.save(user);
    }

    public void generateStudios() {
        if (!studioRepository.findAll().isEmpty() && !profileImageRepository.findAll().isEmpty() && !studioActivityRepository.findAll().isEmpty()) {

        } else {
            generateStudiosData();
            generateAdditionalStudios();
            generateGroupSportsActivities();
        }

    }

    private void generateGroupSportsActivities() {
        List<Studio> studios = studioRepository.findAll();

        if (studios.isEmpty()) {
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
                    .build();

            var a = currentStudio.getStudioActivities();
            if (a != null) {
                currentStudio.getStudioActivities().add(activity);
            }
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

    protected void generateStudiosData() {

        List<Instructor> instructors = getInstructors();

        Studio yogaStudio = Studio.builder()
                .name("Tranquility Yoga Center")
                .description("A serene space dedicated to enhancing your mind and body connection through yoga.")
                .location("Stephansplatz 3, 1010 Vienna, Austria")
                .approved(true)
                .instructors(new ArrayList<>(Arrays.asList(instructors.get(0), instructors.get(1))))
                .profileImage(ProfileImage.builder()
                        .name("tranquility_yoga_center")
                        .path("https://www.traineressentials.com/wp-content/uploads/2020/12/Colourful-Personal-Training-Studio-Design.jpg")
                        .build())
                .galleryImages(new ArrayList<>(Arrays.asList(
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
                )))
                .studioActivities(new ArrayList<>(Arrays.asList(
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
                )))
                .studioAdmin(null)
                .build();

        Studio fitnessCenter = Studio.builder()
                .name("Dynamic Fitness Hub")
                .description("An all-in-one fitness center catering to group workouts and personal training.")
                .location("Schönbrunner Schloßstraße 47, 1130 Vienna, Austria")
                .approved(true)
                .instructors(new ArrayList<>(Arrays.asList(instructors.get(2), instructors.get(3), instructors.get(9))))
                .profileImage(ProfileImage.builder()
                        .name("dynamic_fitness_hub")
                        .path("https://as1.ftcdn.net/v2/jpg/03/29/60/84/1000_F_329608479_vP9nFK795X8lWmoTa8DPhMgoewQ7U1lG.jpg")
                        .build())
                .galleryImages(new ArrayList<>(Arrays.asList(
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
                )))
                .studioActivities(new ArrayList<>(Arrays.asList(
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
                )))
                .studioAdmin(null)
                .build();

        Studio sportsArena = Studio.builder()
                .name("Victory Sports Arena")
                .description("A hub for ball sports enthusiasts, offering courts and coaching.")
                .location("Prinz-Eugen-Straße 27, 1030 Vienna, Austria")
                .approved(true)
                .instructors(new ArrayList<>(Arrays.asList(instructors.get(4), instructors.get(5), instructors.get(6))))
                .profileImage(ProfileImage.builder()
                        .name("victory_sports_arena")
                        .path("https://i.pinimg.com/550x/66/19/af/6619afddaf9c3b6d66cda7948c1e9692.jpg")
                        .build())
                .galleryImages(new ArrayList<>(Arrays.asList(
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
                )))
                .studioActivities(new ArrayList<>(Arrays.asList(
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
                                        .path("https://media.istockphoto.com/id/489699302/photo/women-playing-volleyball.jpg?s=612x612&w=0&k=20&c=AH_XOBG3dFQvxlm66s7VjQ4z9FF8w4uGAmb4G9EdnR4=")
                                        .build())
                                .capacity(20)
                                .type(ActivityType.BALL_SPORTS)
                                .build(),
                        StudioActivity.builder()
                                .name("Tennis Tournament")
                                .description("An exciting tennis tournament for enthusiasts.")
                                .dateTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0))
                                .duration(120.0f)
                                .price(35.0f)
                                .preferences(createDefaultPreferencesForActivityType(ActivityType.BALL_SPORTS))
                                .instructor(instructors.get(6))
                                .profileImage(ProfileImage.builder()
                                        .name("tennis_tournament")
                                        .path("https://storage.googleapis.com/splendid-84db4.appspot.com/uploads/ball_sports/tennis.jpg")
                                        .build())
                                .capacity(16)
                                .type(ActivityType.BALL_SPORTS)
                                .build()
                )))
                .studioAdmin(null)
                .build();
        studioRepository.save(yogaStudio);
        var a = studioRepository.saveAll(Arrays.asList(yogaStudio, fitnessCenter, sportsArena));
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
        studioActivityPreferencesRepository.save(preferences);
        return preferences;
    }

    protected void generateAdditionalStudios() {
        List<String> studioNames = Arrays.asList(
                "Zen Retreat Studio", "Peak Performance Gym", "Harmony Wellness Hub", "Infinite Motion Studio",
                "Vitality Fitness Center", "Energize Wellness Studio", "Balance Yoga Place", "Strength Arena",
                "Core Power Studio", "Elevate Fitness Zone", "Pulse Health Hub", "Momentum Fitness Center",
                "Aspire Studio", "Synergy Health Club", "Renewal Studio", "Revive Wellness Place",
                "Excel Fitness Arena", "Ignite Studio", "Ascend Wellness Zone", "Dynamic Core Hub",
                "Trailblazer Studio", "Powerhouse Fitness", "Prime Studio", "Fortitude Wellness Place",
                "Velocity Studio"
        );

        List<Studio> additionalStudios = new ArrayList<>();

        for (int i = 0; i < studioNames.size(); i++) {
            Studio studio = Studio.builder()
                    .name(studioNames.get(i))
                    .description("A unique fitness and wellness space tailored for personal growth and community.")
                    .location("Studio Street " + (i + 1) + ", Vienna")
                    .approved(true)
                    .studioAdmin(null)
                    .build();

            additionalStudios.add(studio);
        }

        studioRepository.saveAll(additionalStudios);
    }

    private List<StudioActivity> generateActivities(int count) {
        List<StudioActivity> activities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            activities.add(createActivity());
        }
        return activities;
    }

    private StudioActivity createActivity() {
        StudioActivity activity = new StudioActivity();
        activity.setName(randomElement(ACTIVITY_NAMES));
        activity.setDescription(randomElement(DESCRIPTIONS));
        activity.setDateTime(LocalDateTime.now().plusDays(randomInt(1, 30))); // Activity within the next 30 days
        activity.setDuration(randomInt(30, 120)); // Duration between 30 and 120 minutes
        activity.setPrice(randomInt(10, 50)); // Price between 10 and 50
        activity.setType(randomElement(ACTIVITY_TYPES));
        activity.setCapacity(randomInt(10, 50)); // Capacity between 10 and 50

        // Assign preferences
        activity.setPreferences(createActivityPreferences());

        return activity;
    }

    private StudioActivityPreferences createActivityPreferences() {

        StudioActivityPreferences preferences = new StudioActivityPreferences();
        String activitySetting = randomElement(Arrays.asList("Indoor", "Outdoor", "Both"));
        switch (activitySetting) {
            case "Indoor":
                preferences.setIndoor(true);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(false);
                break;
            case "Outdoor":
                preferences.setIndoor(false);
                preferences.setOutdoor(true);
                preferences.setBothIndoorAndOutdoor(false);
                break;
            default:
                preferences.setIndoor(false);
                preferences.setOutdoor(false);
                preferences.setBothIndoorAndOutdoor(true);
                break;
        }
        preferences.setPhysicalDemandLevel(randomInt(1, 10));
        preferences.setSuitableWarmClimate(true);
        preferences.setSuitableColdClimate(randomBoolean());
        preferences.setRainCompatibility(randomBoolean());
        preferences.setWindSuitability(randomBoolean());
        preferences.setInvolvesUpperBody(true);
        preferences.setInvolvesLowerBody(randomBoolean());
        preferences.setInvolvesCore(randomBoolean());
        preferences.setInvolvesFullBody(randomBoolean());
        preferences.setBeginner(true);
        preferences.setIntermediate(randomBoolean());
        preferences.setAdvanced(randomBoolean());
        preferences.setPhysicalDemandLevel(randomInt(1, 10));
        preferences.setGoalStrength(true);
        preferences.setGoalEndurance(randomBoolean());
        preferences.setGoalFlexibility(randomBoolean());
        preferences.setGoalBalanceCoordination(randomBoolean());
        preferences.setGoalMentalFocus(randomBoolean());

        return preferences;
    }

    private static <T> T randomElement(List<T> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    private static int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static boolean randomBoolean() {
        return new Random().nextBoolean();
    }

}
