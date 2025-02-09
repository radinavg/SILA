package com.inso.sila.endpoint.exceptionhandler.errorrestdtos;

import java.util.List;

public record ConflictErrorRestDto(
        String message,
        List<String> errors
) {
}
