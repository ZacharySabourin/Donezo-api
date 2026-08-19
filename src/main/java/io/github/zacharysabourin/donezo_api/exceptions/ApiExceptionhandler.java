package io.github.zacharysabourin.donezo_api.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.github.zacharysabourin.donezo_api.exceptions.models.InternalServerErrorException;
import io.github.zacharysabourin.donezo_api.exceptions.models.NotFoundException;

/**
 * A custom exception handler that hooks into Spring's own exception handling
 * mechanism. Used to handle the custom exceptions created for this application.
 * Extends {@link ResponseEntityExceptionHandler}
 */
@ControllerAdvice
public class ApiExceptionhandler extends ResponseEntityExceptionHandler {

    /**
     * Customize the handling of {@link NotFoundException}.
     * <p>
     * This method delegates to {@link #handleExceptionInternal}.
     * 
     * @param ex      the exception to handle
     * @param request the current request
     * @return a {@code ResponseEntity} for the response to use, possibly
     *         {@code null} when the response is already committed
     */
    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    public @Nullable ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Customize the handling of {@link InternalServerErrorException}.
     * <p>
     * This method delegates to {@link #handleExceptionInternal}.
     * 
     * @param ex      the exception to handle
     * @param request the current request
     * @return a {@code ResponseEntity} for the response to use, possibly
     *         {@code null} when the response is already committed
     */
    @ResponseBody
    @ExceptionHandler(InternalServerErrorException.class)
    public @Nullable ResponseEntity<Object> handleInternalServerErrorException(InternalServerErrorException ex,
            WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
