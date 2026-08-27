package io.github.zacharysabourin.donezo_api.response;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

import java.io.IOException;

import org.springframework.stereotype.Component;

import io.github.zacharysabourin.donezo_api.config.AccessControlConfiguration;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A custom response filter for adding <code>Access-Control-Allow</code> headers
 * to outbound requests. Filter is enabled and disabled through the
 * <code>donezo.access-control-allow.enabled</code> value in the
 * <code>applicaton.properties</code> file.
 */
@Component
public class CORSResponseHeaderFilter implements Filter {

    private final AccessControlConfiguration config;

    public CORSResponseHeaderFilter(AccessControlConfiguration config) {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (Boolean.TRUE.equals(config.enabled())) {
            chain.doFilter(request, response);

            HttpServletResponse servletResponse = (HttpServletResponse) response;
            servletResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, config.origin());
            servletResponse.addHeader(ACCESS_CONTROL_ALLOW_METHODS, config.methods());
            servletResponse.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, config.headers());
        }
    }

}
