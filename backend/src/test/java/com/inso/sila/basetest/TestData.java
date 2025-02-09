package com.inso.sila.basetest;

import java.util.ArrayList;
import java.util.List;

public interface TestData {

    String BASE_URI = "/api/v1";
    String APPLICATION_USER_BASE = BASE_URI + "/users";
    String AUTHENTICATION_BASE = BASE_URI + "/authentication";
    String STUDIO_BASE = BASE_URI + "/studios";
    String REVIEW_BASE = BASE_URI + "/reviews";
    String ACTIVITY_BASE = BASE_URI + "/studio-activities";
    String MEMBERSHIP_BASE = BASE_URI + "/memberships";

    String DEFAULT_USER = "admin@email.com";

    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_USER");
        }
    };


    List<String> USER_ROLES = new ArrayList<>() {
        {
            add("ROLE_USER");
        }
    };

    List<String> STUDIO_ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_STUDIO_ADMIN");
            add("ROLE_USER");
        }
    };
}
