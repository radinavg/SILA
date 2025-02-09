package com.inso.sila.endpoint.exceptionhandler;

import com.inso.sila.endpoint.exceptionhandler.errorrestdtos.ConflictErrorRestDto;
import com.inso.sila.endpoint.exceptionhandler.errorrestdtos.NotFoundErrorRestDto;
import com.inso.sila.endpoint.exceptionhandler.errorrestdtos.ValidationErrorRestDto;
import com.inso.sila.exception.ConflictException;
import com.inso.sila.exception.NotFoundException;
import com.inso.sila.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.invoke.MethodHandles;

@RestControllerAdvice
public class ApplicationExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ValidationErrorRestDto handleValidationException(ValidationException e) {
        LOG.warn("Terminating request processing with status 422 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new ValidationErrorRestDto(e.summary(), e.errors());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ConflictErrorRestDto handleConflictException(ConflictException e) {
        LOG.warn("Terminating request processing with status 409 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new ConflictErrorRestDto(e.summary(), e.errors());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public NotFoundErrorRestDto handleNotFoundException(NotFoundException e) {
        LOG.warn("Terminating request processing with status 404 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return new NotFoundErrorRestDto("Not found");
    }
}