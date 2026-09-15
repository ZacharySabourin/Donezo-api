package io.github.zacharysabourin.donezo_api.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.github.zacharysabourin.donezo_api.exceptions.models.BadRequestException;
import io.github.zacharysabourin.donezo_api.exceptions.models.InternalServerErrorException;
import io.github.zacharysabourin.donezo_api.exceptions.models.NotFoundException;

/**
 * Global exception handler that intercepts application-specific and Spring
 * framework exceptions.
 * Extends {@link ResponseEntityExceptionHandler} to provide centralized HTTP
 * response translation.
 */
@ControllerAdvice
public class ApiExceptionhandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link NotFoundException} by translating it to a
     * {@code 404 Not Found} response.
     *
     * @param ex      the caught exception
     * @param request the active web request
     * @return a {@link ResponseEntity} containing the formatted response, or
     *         {@code null} if committed
     */
    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    public @Nullable ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles {@link InternalServerErrorException} by translating it to a
     * {@code 500 Internal Server Error} response.
     *
     * @param ex      the caught exception
     * @param request the active web request
     * @return a {@link ResponseEntity} containing the formatted response, or
     *         {@code null} if committed
     */
    @ResponseBody
    @ExceptionHandler(InternalServerErrorException.class)
    public @Nullable ResponseEntity<Object> handleInternalServerErrorException(InternalServerErrorException ex,
            WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Handles {@link BadRequestException} by translating it to a
     * {@code 400 Bad Request} response.
     *
     * @param ex      the caught exception
     * @param request the active web request
     * @return a {@link ResponseEntity} containing the formatted response, or
     *         {@code null} if committed
     */
    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    public @Nullable ResponseEntity<Object> handleBadRequestException(BadRequestException ex,
            WebRequest request) {
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles persistence-layer {@link DataAccessException} by logging the database
     * error and returning a {@code 500 Internal Server Error} response.
     *
     * @param ex      the database exception
     * @param request the active web request
     * @return a {@link ResponseEntity} containing the formatted response, or
     *         {@code null} if committed
     */
    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    public @Nullable ResponseEntity<Object> handleDataAccessException(DataAccessException ex,
            WebRequest request) {
        logger.error("Database operation failed", ex);
        return handleExceptionInternal(ex, null, HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Handles authentication failures by generating an RFC 7807
     * {@link ProblemDetail} payload with a {@code 401 Unauthorized} status.
     *
     * @param ex the authentication exception
     * @return a populated {@link ProblemDetail} detailing the authentication
     *         failure
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
