package io.github.zacharysabourin.donezo_api.exceptions.models;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

import jakarta.servlet.ServletException;

/**
 * A custom exception for <code>500 Internal Server Error</code> responses.
 * Created to fit the RFC 9457 specification and hook into Spring's own error
 * response handling.
 */
public class InternalServerErrorException extends ServletException implements ErrorResponse {

    private final HttpMethod httpMethod;
    private final ProblemDetail body;

    public InternalServerErrorException(String message, HttpMethod httpMethod) {
        super(message);
        this.httpMethod = httpMethod;
        this.body = ProblemDetail.forStatusAndDetail(getStatusCode(), message);
    }

    /**
     * Return the HTTP method for use in the response.
     */
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public ProblemDetail getBody() {
        return this.body;
    }
}