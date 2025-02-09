package com.inso.sila.datagenerator;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.ApplicationUserPreferences;
import com.inso.sila.entity.FriendRequest;
import com.inso.sila.enums.Gender;
import com.inso.sila.enums.RequestStatus;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FriendRequestRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class UserDataGenerator {
    private final FriendRequestRepository friendRequestRepository;

    private String password = "password";
    private final ApplicationUserRepository userRepository;

    public UserDataGenerator(ApplicationUserRepository userRepository, PasswordEncoder passwordEncoder, FriendRequestRepository friendRequestRepository) {
        this.userRepository = userRepository;
        this.password = passwordEncoder.encode(password);
        this.friendRequestRepository = friendRequestRepository;
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

    public void generateAdditionalUsers(int numberOfUsers) {
        List<String> firstNames = Arrays.asList(
                "Emma", "Olivia", "Ava", "Isabella", "Sophia",
                "Mia", "Charlotte", "Amelia", "Evelyn", "Abigail",
                "Harper", "Emily", "Elizabeth", "Avery", "Sofia",
                "Ella", "Madison", "Scarlett", "Victoria", "Aria",
                "Grace", "Chloe", "Camila", "Penelope", "Riley",
                "Layla", "Lillian", "Nora", "Zoey", "Hannah",
                "Aubrey", "Addison", "Ellie", "Stella", "Natalie",
                "Zoe", "Leah", "Hazel", "Violet", "Aurora"
        );

        List<String> lastNames = Arrays.asList(
                "Smith", "Johnson", "Williams", "Brown", "Jones",
                "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                "Thomas", "Taylor", "Moore", "Jackson", "Martin",
                "Lee", "Perez", "Thompson", "White", "Harris",
                "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
                "Walker", "Young", "Allen", "King", "Wright",
                "Scott", "Torres", "Nguyen", "Hill", "Flores"
        );

        List<String> domains = Arrays.asList(
                "gmail.com", "outlook.com", "yahoo.at"
        );

        for (int i = 0; i < numberOfUsers; i++) {
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
                    .isLocked(i % 3 == 0)
                    .loginAttempts(i % 3 == 0 ? 6 : 0)
                    .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                    .location("Street " + (i + 1) + ", Vienna")
                    .latitude(latitude + random)
                    .longitude(longitude + random)
                    .preferencesSet(true)
                    .build();

            user = userRepository.save(user);
            ApplicationUserPreferences applicationUserPreferences = ApplicationUserPreferences.builder()
                    .user(user)
                    .prefersIndividual(randomBoolean())
                    .prefersTeam(randomBoolean())
                    .prefersWaterBased(randomBoolean())
                    .prefersIndoor(randomBoolean())
                    .prefersOutdoor(randomBoolean())
                    .prefersBothIndoorAndOutdoor(randomBoolean())
                    .prefersWarmClimate(randomBoolean())
                    .prefersColdClimate(randomBoolean())
                    .rainCompatibility(randomBoolean())
                    .windSuitability(randomBoolean())
                    .focusUpperBody(randomBoolean())
                    .focusCore(randomBoolean())
                    .focusFullBody(randomBoolean())
                    .isBeginner(randomBoolean())
                    .isIntermediate(randomBoolean())
                    .isAdvanced(randomBoolean())
                    .physicalDemandLevel(randomInt(10))
                    .goalStrength(randomBoolean())
                    .goalEndurance(randomBoolean())
                    .goalFlexibility(randomBoolean())
                    .goalBalanceCoordination(randomBoolean())
                    .goalMentalFocus(randomBoolean()).build();
            user.setPreferences(applicationUserPreferences);
            userRepository.save(user);
        }

    }

    private static boolean randomBoolean() {
        return new Random().nextBoolean();
    }

    private static int randomInt(int max) {
        return new Random().nextInt(max - 1 + 1) + 1;
    }
}
