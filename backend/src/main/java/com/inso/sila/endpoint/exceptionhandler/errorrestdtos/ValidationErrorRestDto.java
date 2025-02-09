package com.inso.sila.endpoint.exceptionhandler.errorrestdtos;

import java.util.List;

public record ValidationErrorRestDto(
        String message,
        List<String> errors
) {
}