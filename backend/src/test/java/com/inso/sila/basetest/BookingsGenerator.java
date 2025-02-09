package com.inso.sila.basetest;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Review;
import com.inso.sila.entity.Studio;
import com.inso.sila.entity.StudioActivity;
import com.inso.sila.repository.ApplicationUserRepository;
import com.inso.sila.repository.ReviewRepository;
import com.inso.sila.repository.StudioActivityRepository;
import com.inso.sila.repository.StudioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class BookingsGenerator {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private StudioRepository studioRepository;
    @Autowired
    private StudioActivityRepository studioActivityRepository;

    @Transactional
    public void generateStudioReviews() {

        List<ApplicationUser> users = applicationUserRepository.findAll();
        Random random = new Random();

        for (ApplicationUser user : users) {
            List<Studio> studiosForReview = studioRepository.findStudiosForWhichUserHasBookings(user.getApplicationUserId());
            for (Studio studio : studiosForReview) {
                Review review = Review.builder()
                        .text("My review text")
                        .rating(random.nextInt(5 - 1 + 1) + 1)
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .studio(studio)
                        .build();
                var a = reviewRepository.save(review);
                int i = 123;
            }
        }
    }

    @Transactional
    public void generateRandomBookings() {

        List<ApplicationUser> users = applicationUserRepository.findAll();
        List<StudioActivity> activities = studioActivityRepository.findAll();

        Random random = new Random();

        for (ApplicationUser user : users) {
            List<StudioActivity> userActivities = new ArrayList<>();

            int activityCount = random.nextInt(5) + 1;

            while (userActivities.size() < activityCount) {
                int randomIndex = random.nextInt(activities.size());
                StudioActivity randomActivity = activities.get(randomIndex);

                if (!userActivities.contains(randomActivity)) {
                    userActivities.add(randomActivity);
                }
            }
            user.setStudioActivities(userActivities);
            var a =  applicationUserRepository.save(user);
            int aa= 23;
        }
    }
}