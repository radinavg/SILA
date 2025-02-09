package com.inso.sila.endpoint;

import com.inso.sila.endpoint.dto.requests.NotificationsDto;
import com.inso.sila.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final NotificationsService notificationsService;


    @Secured("ROLE_USER")
    @GetMapping
    public ResponseEntity<NotificationsDto> getAllUnprocessedNotifications() {
        LOG.info("getAllUnprocessedNotifications()");
        return ResponseEntity.ok(notificationsService.getAllUnprocessedNotifications());
    }
}
