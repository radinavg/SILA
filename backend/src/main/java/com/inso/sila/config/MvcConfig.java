package com.inso.sila.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String resourcesPathStudioActivityProfileImages = new File("assets/studio-activity/profile-images").getAbsolutePath();

        registry.addResourceHandler("assets/studio-activity/profile-images/**")
                .addResourceLocations("file:" + resourcesPathStudioActivityProfileImages + "/")
                .setCachePeriod(3600)
                .resourceChain(true);


        String resourcesPathStudioProfileImages = new File("assets/studio/profile-images").getAbsolutePath();
        registry.addResourceHandler("assets/studio/profile-images/**")
                .addResourceLocations("file:" + resourcesPathStudioProfileImages + "/")
                .setCachePeriod(3600)
                .resourceChain(true);


        String resourcesPathStudioGalleryImages = new File("assets/studio/gallery-images").getAbsolutePath();
        registry.addResourceHandler("assets/studio/gallery-images/**")
                .addResourceLocations("file:" + resourcesPathStudioGalleryImages + "/")
                .setCachePeriod(3600)
                .resourceChain(true);


        String resourcesPathUserProfileImages = new File("assets/user/profile-images").getAbsolutePath();
        registry.addResourceHandler("assets/user/profile-images/**")
                .addResourceLocations("file:" + resourcesPathUserProfileImages + "/")
                .setCachePeriod(3600)
                .resourceChain(true);

        String resourcesPathInstructorProfileImage = new File("assets/instructor-profile-images/").getAbsolutePath();

        registry.addResourceHandler("assets/instructor-profile-images/**")
                .addResourceLocations("file:" + resourcesPathInstructorProfileImage + "/")
                .setCachePeriod(3600)
                .resourceChain(true);

    }
}
