package com.inso.sila.endpoint.dto.user;

public record UserSearchDto(
        String firstName,
        String lastName,
        String email,
        Boolean isAdmin,
        Boolean isLocked,
        Integer pageIndex,
        Integer pageSize
) {
}