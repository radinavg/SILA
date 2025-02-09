package com.inso.sila.integrationtest;

import com.inso.sila.SilaApplication;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SilaApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityTest {
    private static final List<Class<?>> mappingAnnotations = Lists.list(
            RequestMapping.class,
            GetMapping.class,
            PostMapping.class,
            PutMapping.class,
            PatchMapping.class,
            DeleteMapping.class
    );

    private static final List<Class<?>> securityAnnotations = Lists.list(
            Secured.class,
            PreAuthorize.class,
            RolesAllowed.class,
            PermitAll.class,
            DenyAll.class,
            DeclareRoles.class
    );

    @Autowired
    private List<Object> components;

    @Test
    void ensureSecurityAnnotationPresentForEveryEndpoint() {
        List<ImmutablePair<Class<?>, Method>> notSecured = components.stream()
                .map(AopUtils::getTargetClass) // beans may be proxies, get the target class instead
                .filter(clazz -> clazz.getCanonicalName() != null && clazz.getCanonicalName().startsWith(SilaApplication.class.getPackageName())) // limit to our package
                .filter(clazz -> clazz.getAnnotation(RestController.class) != null) // limit to classes annotated with @RestController
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()).map(method -> new ImmutablePair<Class<?>, Method>(clazz, method))) // get all class -> method pairs
                .filter(pair -> Arrays.stream(pair.getRight().getAnnotations()).anyMatch(annotation -> mappingAnnotations.contains(annotation.annotationType()))) // keep only the pairs where the method has a "mapping annotation"
                .filter(pair -> Arrays.stream(pair.getRight().getAnnotations()).noneMatch(annotation -> securityAnnotations.contains(annotation.annotationType()))) // keep only the pairs where the method does not have a "security annotation"
                .toList();

        assertThat(notSecured.size())
                .as("Most rest methods should be secured. If one is really intended for public use, explicitly state that with @PermitAll. "
                        + "The following are missing: \n" + notSecured.stream().map(pair -> "Class: " + pair.getLeft() + " Method: " + pair.getRight()).reduce("", (a, b) -> a + "\n" + b))
                .isZero();

    }
}
