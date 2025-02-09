package com.inso.sila.datagenerator;

import com.inso.sila.repository.ActivityTypeAttributesRepository;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.FaqsRepository;
import com.inso.sila.repository.FriendRequestRepository;
import com.inso.sila.repository.MembershipRepository;
import com.inso.sila.repository.ProfileImageRepository;
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

@Profile({"datagen-large"})
@Component
public class GeneratorLargeInstance {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudioRepository studioRepository;
    private final ProfileImageRepository profileImageRepository;
    private final StudioActivityRepository studioActivityRepository;
    private final FaqsRepository faqsRepository;
    private final MembershipRepository membershipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ActivityTypeAttributesRepository attributesRepository;
    private final StudioActivityPreferencesRepository preferencesRepository;
    private final ReviewRepository reviewRepository;
    private final WebClient webClient;
    private static final String CLUSTERING_API = "http://datascience:5000";

    public GeneratorLargeInstance(ApplicationUserRepository userRepository, PasswordEncoder passwordEncoder, StudioRepository studioRepository,
                                  ProfileImageRepository profileImageRepository, StudioActivityRepository studioActivityRepository,
                                  FriendRequestRepository friendRequestRepository,
                                  FaqsRepository faqsRepository,
                                  MembershipRepository membershipRepository, ActivityTypeAttributesRepository attributesRepository,
                                  StudioActivityPreferencesRepository preferencesRepository, ReviewRepository reviewRepository,
                                  WebClient.Builder builder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studioRepository = studioRepository;
        this.profileImageRepository = profileImageRepository;
        this.studioActivityRepository = studioActivityRepository;
        this.faqsRepository = faqsRepository;
        this.membershipRepository = membershipRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.attributesRepository = attributesRepository;
        this.preferencesRepository = preferencesRepository;
        this.reviewRepository = reviewRepository;
        this.webClient = builder.baseUrl(CLUSTERING_API).build();
    }

    @PostConstruct
    private void generateData() {
        LOG.info("Generating data...");
        UserDataGenerator userDataGenerator = new UserDataGenerator(userRepository, passwordEncoder, friendRequestRepository);
        if (!userRepository.findAll().isEmpty()) {
            LOG.info("Users already generated");
        } else {
            LOG.info("Generating user entries...");
            userDataGenerator.generateUsers();
            userDataGenerator.generateAdditionalUsers(1000);
        }
        StudioAndActivityDataGenerator studioAndActivityDataGenerator = new StudioAndActivityDataGenerator(studioActivityRepository, studioRepository,
                profileImageRepository, faqsRepository, membershipRepository, userRepository, preferencesRepository, reviewRepository, passwordEncoder);
        studioAndActivityDataGenerator.generateStudios();
        ActivityTypeAttributesGenerator attributesGenerator = new ActivityTypeAttributesGenerator(attributesRepository, preferencesRepository);
        attributesGenerator.generateAttributes();
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
