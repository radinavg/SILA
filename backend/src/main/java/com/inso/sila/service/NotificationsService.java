package com.inso.sila.service;

import com.inso.sila.endpoint.dto.requests.NotificationsDto;

public interface NotificationsService {

    /**
     * Gets all unprocessed notifications, which could be
     * activity invitations and/or friendship requests.
     *
     * @return a notifications dto, consisting of invitations and requests.
     */
    NotificationsDto getAllUnprocessedNotifications();
}
