package io.github.zacharysabourin.donezo_api.request;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom filter that enforces deferred initialization and persistence of the
 * CSRF token cookie.
 * <p>
 * Evaluates the request attribute for a {@link CsrfToken} on every HTTP request
 * and accesses the token to force Spring Security's repository to render and
 * send the {@code XSRF-TOKEN} cookie.
 */
@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    /**
     * {@inheritDoc}
     * <p>
     * Eagerly resolves the CSRF token from the request attribute, prompting the
     * cookie repository to write the token value to the client response prior to
     * execution of the remaining filter chain.
     *
     * @param request     the incoming HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the Spring Security filter chain
     * @throws ServletException if a servlet processing error occurs
     * @throws IOException      if an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (null != csrfToken) {
            // Accessing the token forces Spring to persist it as a cookie
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}